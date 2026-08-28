package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppLanguage
import com.example.core.localization.AppStrings
import com.example.core.stats.UserStats
import com.example.model.SavedMediaItem
import com.example.model.SavedMessage
import com.example.ui.components.HeaderUserStatsCounter
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.GoldGradientColors
import com.example.ui.theme.ImmersiveGold
import com.example.ui.viewmodel.DashboardTab
import com.example.ui.viewmodel.MessageFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    themeMode: AppThemeMode,
    language: AppLanguage,
    currentTab: DashboardTab,
    messages: List<SavedMessage>,
    mediaList: List<SavedMediaItem> = emptyList(),
    searchQuery: String,
    activeFilter: MessageFilter,
    selectedMessage: SavedMessage?,
    isDecoyVaultActive: Boolean,
    isOfflineModeEnabled: Boolean,
    primaryPin: String,
    vaultPin: String,
    isPrimaryPinSet: Boolean,
    isVaultPinSet: Boolean,
    isAppLocked: Boolean,
    userStats: UserStats = UserStats(),
    onSelectTab: (DashboardTab) -> Unit,
    onThemeChange: (AppThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onOfflineModeChange: (Boolean) -> Unit,
    onSearchChange: (String) -> Unit,
    onFilterChange: (MessageFilter) -> Unit,
    onSelectMessage: (SavedMessage?) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onDeleteMedia: (String) -> Unit = {},
    onSetPrimaryPin: (String, Boolean) -> Unit,
    onRemovePrimaryPin: () -> Unit,
    onSetVaultPin: (String, Boolean) -> Unit,
    onRemoveVaultPin: () -> Unit,
    onClearAllChats: () -> Unit,
    onLockApp: () -> Unit,
    onUnlockWithPin: (String) -> Boolean,
    onReplaySplash: () -> Unit
) {
    val strings = remember(language) { AppStrings.get(language) }

    val goldBrush = Brush.linearGradient(
        colors = GoldGradientColors,
        start = Offset(0f, 0f),
        end = Offset(200f, 200f)
    )

    // Native Edge-Swipe & Gesture Navigation support
    BackHandler(enabled = !isAppLocked && (selectedMessage != null || searchQuery.isNotEmpty() || currentTab != DashboardTab.CHATS)) {
        if (selectedMessage != null) {
            onSelectMessage(null)
        } else if (searchQuery.isNotEmpty()) {
            onSearchChange("")
        } else if (currentTab != DashboardTab.CHATS) {
            onSelectTab(DashboardTab.CHATS)
        }
    }

    if (isAppLocked) {
        PinLockScreen(
            language = language,
            onUnlockWithPin = onUnlockWithPin
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Immersive mini-badge & Brand Title
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .rotate(-3f)
                                        .size(34.dp)
                                        .shadow(
                                            elevation = 8.dp,
                                            shape = RoundedCornerShape(10.dp),
                                            ambientColor = ImmersiveGold,
                                            spotColor = ImmersiveGold
                                        )
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(goldBrush)
                                ) {
                                    Text(
                                        text = "DSZ",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = (-0.5).sp,
                                        color = Color(0xFF121212)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "DSZ ",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "Save Chat",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = ImmersiveGold
                                        )
                                    }
                                    // Authentic Subtitle: Zero mention of "Vault" or "Decoy"
                                    Text(
                                        text = "by M WASIF DSZ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            // Right: TikTok-Style 3-Step Live Header Stats Counter
                            HeaderUserStatsCounter(
                                stats = userStats,
                                themeMode = themeMode,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Safe Banner Ad placed safely right above the bottom navigation bar
                    AdBannerCard(
                        language = language,
                        isOfflineModeEnabled = isOfflineModeEnabled
                    )

                    // Bottom Navigation Bar (Chats, Media, Settings)
                    DSZBottomNavigationBar(
                        currentTab = currentTab,
                        language = language,
                        onSelectTab = onSelectTab
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "dashboard_tab_transition"
                ) { tab ->
                    when (tab) {
                        DashboardTab.CHATS -> {
                            ChatListScreen(
                                language = language,
                                messages = messages,
                                searchQuery = searchQuery,
                                activeFilter = activeFilter,
                                selectedMessage = selectedMessage,
                                isDecoyVaultActive = isDecoyVaultActive,
                                onSearchChange = onSearchChange,
                                onFilterChange = onFilterChange,
                                onSelectMessage = onSelectMessage,
                                onDeleteMessage = onDeleteMessage
                            )
                        }
                        DashboardTab.MEDIA -> {
                            MediaGalleryScreen(
                                language = language,
                                mediaList = mediaList,
                                isDecoyVaultActive = isDecoyVaultActive,
                                onDeleteMedia = onDeleteMedia
                            )
                        }
                        DashboardTab.SETTINGS -> {
                            SettingsScreen(
                                themeMode = themeMode,
                                language = language,
                                isOfflineModeEnabled = isOfflineModeEnabled,
                                primaryPin = primaryPin,
                                vaultPin = vaultPin,
                                isPrimaryPinSet = isPrimaryPinSet,
                                isVaultPinSet = isVaultPinSet,
                                isDecoyVaultActive = isDecoyVaultActive,
                                onThemeChange = onThemeChange,
                                onLanguageChange = onLanguageChange,
                                onOfflineModeChange = onOfflineModeChange,
                                onSetPrimaryPin = onSetPrimaryPin,
                                onRemovePrimaryPin = onRemovePrimaryPin,
                                onSetVaultPin = onSetVaultPin,
                                onRemoveVaultPin = onRemoveVaultPin,
                                onClearAllChats = onClearAllChats,
                                onReplaySplash = onReplaySplash
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Luxury Immersive Bottom Navigation Bar
 * Features 3 core tabs: Chats (💬), Media (🖼️), and Settings (⚙️)
 */
@Composable
fun DSZBottomNavigationBar(
    currentTab: DashboardTab,
    language: AppLanguage,
    onSelectTab: (DashboardTab) -> Unit
) {
    val strings = remember(language) { AppStrings.get(language) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                ambientColor = ImmersiveGold.copy(alpha = 0.2f),
                spotColor = ImmersiveGold.copy(alpha = 0.3f)
            )
            .testTag("dsz_bottom_navigation_bar"),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Chats (💬)
            BottomNavItem(
                title = strings.tabChats,
                selectedIcon = Icons.Filled.Chat,
                unselectedIcon = Icons.Outlined.ChatBubbleOutline,
                isSelected = currentTab == DashboardTab.CHATS,
                testTag = "tab_chats",
                onClick = { onSelectTab(DashboardTab.CHATS) }
            )

            // Tab 2: Media (🖼️)
            BottomNavItem(
                title = strings.tabMedia,
                selectedIcon = Icons.Filled.PhotoLibrary,
                unselectedIcon = Icons.Outlined.PhotoLibrary,
                isSelected = currentTab == DashboardTab.MEDIA,
                testTag = "tab_media",
                onClick = { onSelectTab(DashboardTab.MEDIA) }
            )

            // Tab 3: Settings (⚙️)
            BottomNavItem(
                title = strings.tabSettings,
                selectedIcon = Icons.Filled.Settings,
                unselectedIcon = Icons.Outlined.Settings,
                isSelected = currentTab == DashboardTab.SETTINGS,
                testTag = "tab_settings",
                onClick = { onSelectTab(DashboardTab.SETTINGS) }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    title: String,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    val iconTint = if (isSelected) ImmersiveGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val textStyle = if (isSelected) {
        MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = ImmersiveGold)
    } else {
        MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Selected Pill indicator / Icon container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(if (isSelected) 32.dp else 26.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            Color.Transparent
                        }
                    )
            ) {
                Icon(
                    imageVector = if (isSelected) selectedIcon else unselectedIcon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = title,
                style = textStyle,
                fontSize = if (isSelected) 10.5.sp else 10.sp
            )
        }
    }
}
