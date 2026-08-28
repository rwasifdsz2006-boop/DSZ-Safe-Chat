package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.localization.AppLanguage
import com.example.core.stats.UserStats
import com.example.core.stats.UserStatsManager
import com.example.core.notification.NotificationInterceptorEvents
import com.example.core.storage.AppPreferences
import com.example.core.storage.ChatDatabaseHelper
import com.example.model.ChatSource
import com.example.model.SavedMediaItem
import com.example.model.SavedMessage
import com.example.ui.screens.MediaTypeFilter
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class DashboardTab {
    CHATS,
    MEDIA,
    SETTINGS
}

enum class MessageFilter {
    ALL,
    DELETED,
    WHATSAPP,
    WHATSAPP_BUSINESS,
    TELEGRAM,
    DIRECT
}

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = AppPreferences(application)
    private val statsManager = UserStatsManager(application)
    private val dbHelper = ChatDatabaseHelper(application)

    // Production-Grade 3-Step Live User Stats (TikTok Style total, Live Online with pulsing green dot, DAU)
    val userStats: StateFlow<UserStats> = statsManager.stats

    // 3. Theme Mode: Default to Dark Theme (persisted)
    private val _themeMode = MutableStateFlow(prefs.themeMode)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    // 2. Language: English as default main language (persisted)
    private val _language = MutableStateFlow(prefs.language)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private val _currentTab = MutableStateFlow(DashboardTab.CHATS)
    val currentTab: StateFlow<DashboardTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeFilter = MutableStateFlow(MessageFilter.ALL)
    val activeFilter: StateFlow<MessageFilter> = _activeFilter.asStateFlow()

    // 5. Online/Offline Network Mode: Default OFF = Online only (persisted)
    private val _isOfflineModeEnabled = MutableStateFlow(prefs.isOfflineModeEnabled)
    val isOfflineModeEnabled: StateFlow<Boolean> = _isOfflineModeEnabled.asStateFlow()

    // Real messages in database (starts 100% clean & empty)
    private val _realMessages = MutableStateFlow<List<SavedMessage>>(emptyList())
    val messages: StateFlow<List<SavedMessage>> = _realMessages.asStateFlow()

    // Centralized Media items in database
    private val _mediaItems = MutableStateFlow<List<SavedMediaItem>>(emptyList())
    val mediaItems: StateFlow<List<SavedMediaItem>> = _mediaItems.asStateFlow()

    init {
        loadPersistedData()
        observeLiveNotifications()
    }

    private fun observeLiveNotifications() {
        viewModelScope.launch {
            NotificationInterceptorEvents.incomingMessages.collect { newMsg ->
                // Prepend immediately to real messages
                val currentList = _realMessages.value
                if (currentList.none { it.id == newMsg.id }) {
                    _realMessages.value = listOf(newMsg) + currentList
                }
                // Also update media tab if media payload was detected
                if (newMsg.mediaType != null) {
                    val newMedia = SavedMediaItem(
                        id = newMsg.id,
                        title = "${newMsg.mediaType.lowercase().replaceFirstChar { it.uppercase() }} from ${newMsg.senderName}",
                        senderName = newMsg.senderName,
                        type = when (newMsg.mediaType) {
                            "PHOTO" -> MediaTypeFilter.PHOTOS
                            "VIDEO" -> MediaTypeFilter.VIDEOS
                            "AUDIO" -> MediaTypeFilter.AUDIO
                            else -> MediaTypeFilter.ALL
                        },
                        timestamp = newMsg.timestamp,
                        fileSize = "Encrypted Local File",
                        filePath = newMsg.mediaUri,
                        source = newMsg.source.displayName
                    )
                    val currentMediaList = _mediaItems.value
                    if (currentMediaList.none { it.id == newMedia.id }) {
                        _mediaItems.value = listOf(newMedia) + currentMediaList
                    }
                }
            }
        }
    }

    fun loadPersistedData() {
        viewModelScope.launch(Dispatchers.IO) {
            val dbMsgs = dbHelper.getAllMessages()
            val dbMedia = dbHelper.getAllMediaItems()
            _realMessages.value = dbMsgs
            _mediaItems.value = dbMedia
        }
    }

    // Smart Vault State: if true, app is in decoy mode (displays normal app layout, 0 real messages, NO settings access)
    private val _isDecoyVaultActive = MutableStateFlow(false)
    val isDecoyVaultActive: StateFlow<Boolean> = _isDecoyVaultActive.asStateFlow()

    private val _selectedMessage = MutableStateFlow<SavedMessage?>(null)
    val selectedMessage: StateFlow<SavedMessage?> = _selectedMessage.asStateFlow()

    // Security PINs & Startup Lock Flow (persisted in SharedPreferences)
    private val _primaryPin = MutableStateFlow(prefs.primaryPin)
    val primaryPin: StateFlow<String> = _primaryPin.asStateFlow()

    private val _vaultPin = MutableStateFlow(prefs.vaultPin)
    val vaultPin: StateFlow<String> = _vaultPin.asStateFlow()

    private val _isPrimaryLockEnabled = MutableStateFlow(prefs.isPrimaryLockEnabled && prefs.primaryPin.isNotEmpty())
    val isPrimaryLockEnabled: StateFlow<Boolean> = _isPrimaryLockEnabled.asStateFlow()

    private val _isVaultLockEnabled = MutableStateFlow(prefs.isVaultLockEnabled && prefs.vaultPin.isNotEmpty())
    val isVaultLockEnabled: StateFlow<Boolean> = _isVaultLockEnabled.asStateFlow()

    // Startup Lock: If user previously saved a lock password, start locked. Otherwise start directly unlocked.
    private val _isAppLocked = MutableStateFlow(
        (prefs.isPrimaryLockEnabled && prefs.primaryPin.isNotEmpty()) ||
        (prefs.isVaultLockEnabled && prefs.vaultPin.isNotEmpty())
    )
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.themeMode = mode
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
        prefs.language = lang
    }

    fun selectTab(tab: DashboardTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: MessageFilter) {
        _activeFilter.value = filter
    }

    fun selectMessage(msg: SavedMessage?) {
        _selectedMessage.value = msg
    }

    fun setOfflineModeEnabled(enabled: Boolean) {
        _isOfflineModeEnabled.value = enabled
        prefs.isOfflineModeEnabled = enabled
        statsManager.setOfflineMode(enabled)
    }

    // --- PIN & Lock Management ---

    fun setPrimaryPin(pin: String, enabled: Boolean = true) {
        _primaryPin.value = pin
        val isEnabled = enabled && pin.isNotEmpty()
        _isPrimaryLockEnabled.value = isEnabled
        prefs.primaryPin = pin
        prefs.isPrimaryLockEnabled = isEnabled
    }

    fun removePrimaryPin() {
        _primaryPin.value = ""
        _isPrimaryLockEnabled.value = false
        prefs.primaryPin = ""
        prefs.isPrimaryLockEnabled = false
        // If neither lock is enabled, ensure app is unlocked
        if (!_isVaultLockEnabled.value) {
            _isAppLocked.value = false
        }
    }

    fun setVaultPin(pin: String, enabled: Boolean = true) {
        _vaultPin.value = pin
        val isEnabled = enabled && pin.isNotEmpty()
        _isVaultLockEnabled.value = isEnabled
        prefs.vaultPin = pin
        prefs.isVaultLockEnabled = isEnabled
    }

    fun removeVaultPin() {
        _vaultPin.value = ""
        _isVaultLockEnabled.value = false
        prefs.vaultPin = ""
        prefs.isVaultLockEnabled = false
        if (!_isPrimaryLockEnabled.value) {
            _isAppLocked.value = false
        }
    }

    fun lockApp() {
        if ((_isPrimaryLockEnabled.value && _primaryPin.value.isNotEmpty()) ||
            (_isVaultLockEnabled.value && _vaultPin.value.isNotEmpty())) {
            _isAppLocked.value = true
        }
    }

    /**
     * Smart Backend Routing with Single Input Box:
     * - Primary PIN entered -> Unlocks real chats & full Settings access (_isDecoyVaultActive = false)
     * - Vault PIN entered -> Unlocks authentic-looking normal app interface with Settings completely hidden (_isDecoyVaultActive = true)
     * - Returns true if matched, false otherwise.
     */
    fun unlockWithPin(enteredPin: String): Boolean {
        if (_isPrimaryLockEnabled.value && _primaryPin.value.isNotEmpty() && enteredPin == _primaryPin.value) {
            _isDecoyVaultActive.value = false
            _isAppLocked.value = false
            _currentTab.value = DashboardTab.CHATS
            return true
        }
        if (_isVaultLockEnabled.value && _vaultPin.value.isNotEmpty() && enteredPin == _vaultPin.value) {
            _isDecoyVaultActive.value = true
            _isAppLocked.value = false
            _currentTab.value = DashboardTab.CHATS
            return true
        }
        // If locks are disabled, unlock directly
        if (!_isPrimaryLockEnabled.value && !_isVaultLockEnabled.value) {
            _isAppLocked.value = false
            return true
        }
        return false
    }

    fun saveIncomingMessage(
        senderName: String,
        messageText: String,
        source: ChatSource,
        isDeletedBySender: Boolean = false,
        originalDeletedContent: String? = null,
        avatarUri: String? = null,
        mediaType: String? = null,
        mediaUri: String? = null
    ) {
        val letter = senderName.trim().firstOrNull()?.uppercase() ?: "W"
        val newMsg = SavedMessage(
            id = UUID.randomUUID().toString(),
            senderName = senderName,
            senderAvatarLetter = letter,
            messageText = messageText,
            timestamp = "Just now",
            timestampMillis = System.currentTimeMillis(),
            source = source,
            isDeletedBySender = isDeletedBySender,
            originalDeletedContent = originalDeletedContent,
            unread = true,
            avatarUri = avatarUri,
            mediaType = mediaType,
            mediaUri = mediaUri
        )
        _realMessages.value = listOf(newMsg) + _realMessages.value
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.insertMessage(newMsg)
        }
    }

    fun deleteMessage(id: String) {
        _realMessages.value = _realMessages.value.filter { it.id != id }
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.deleteMessage(id)
        }
    }

    fun deleteMedia(id: String) {
        _mediaItems.value = _mediaItems.value.filter { it.id != id }
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.deleteMedia(id)
        }
    }

    fun clearAllChats() {
        _realMessages.value = emptyList()
        _mediaItems.value = emptyList()
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.clearAllMessages()
            dbHelper.clearAllMedia()
        }
    }

    // --- INDIVIDUAL PER-DEVICE LOCAL AD CAPPING ---
    fun canShowInterstitialAd(): Boolean {
        return prefs.canShowInterstitialAd()
    }

    fun recordInterstitialAdImpression() {
        prefs.recordInterstitialAdImpression()
    }

    fun getRemainingDailyAds(): Int {
        return prefs.getRemainingDailyInterstitialAds()
    }
}
