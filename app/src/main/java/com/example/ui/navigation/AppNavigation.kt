package com.example.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.core.localization.AppLanguage
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.viewmodel.AppViewModel

object AppRoutes {
    const val SPLASH = "splash"
    const val HOME = "home"
}

@Composable
fun AppNavigation(
    viewModel: AppViewModel,
    navController: NavHostController = rememberNavController()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val language by viewModel.language.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val mediaItems by viewModel.mediaItems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val activeFilter by viewModel.activeFilter.collectAsState()
    val selectedMessage by viewModel.selectedMessage.collectAsState()
    val isDecoyVaultActive by viewModel.isDecoyVaultActive.collectAsState()
    val isOfflineModeEnabled by viewModel.isOfflineModeEnabled.collectAsState()
    val primaryPin by viewModel.primaryPin.collectAsState()
    val vaultPin by viewModel.vaultPin.collectAsState()
    val isPrimaryPinSet by viewModel.isPrimaryLockEnabled.collectAsState()
    val isVaultPinSet by viewModel.isVaultLockEnabled.collectAsState()
    val isAppLocked by viewModel.isAppLocked.collectAsState()
    val userStats by viewModel.userStats.collectAsState()

    val layoutDirection = if (language == AppLanguage.URDU) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        NavHost(
            navController = navController,
            startDestination = AppRoutes.SPLASH,
            enterTransition = { fadeIn(animationSpec = tween(400)) },
            exitTransition = { fadeOut(animationSpec = tween(400)) }
        ) {
            composable(route = AppRoutes.SPLASH) {
                SplashScreen(
                    language = language,
                    onNavigateToNext = {
                        navController.navigate(AppRoutes.HOME) {
                            popUpTo(AppRoutes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(route = AppRoutes.HOME) {
                HomeScreen(
                    themeMode = themeMode,
                    language = language,
                    currentTab = currentTab,
                    messages = messages,
                    mediaList = mediaItems,
                    searchQuery = searchQuery,
                    activeFilter = activeFilter,
                    selectedMessage = selectedMessage,
                    isDecoyVaultActive = isDecoyVaultActive,
                    isOfflineModeEnabled = isOfflineModeEnabled,
                    primaryPin = primaryPin,
                    vaultPin = vaultPin,
                    isPrimaryPinSet = isPrimaryPinSet,
                    isVaultPinSet = isVaultPinSet,
                    isAppLocked = isAppLocked,
                    userStats = userStats,
                    onSelectTab = { tab -> viewModel.selectTab(tab) },
                    onThemeChange = { mode -> viewModel.setThemeMode(mode) },
                    onLanguageChange = { lang -> viewModel.setLanguage(lang) },
                    onOfflineModeChange = { enabled -> viewModel.setOfflineModeEnabled(enabled) },
                    onSearchChange = { query -> viewModel.setSearchQuery(query) },
                    onFilterChange = { filter -> viewModel.setFilter(filter) },
                    onSelectMessage = { msg -> viewModel.selectMessage(msg) },
                    onDeleteMessage = { id -> viewModel.deleteMessage(id) },
                    onDeleteMedia = { id -> viewModel.deleteMedia(id) },
                    onSetPrimaryPin = { pin, enabled -> viewModel.setPrimaryPin(pin, enabled) },
                    onRemovePrimaryPin = { viewModel.removePrimaryPin() },
                    onSetVaultPin = { pin, enabled -> viewModel.setVaultPin(pin, enabled) },
                    onRemoveVaultPin = { viewModel.removeVaultPin() },
                    onClearAllChats = { viewModel.clearAllChats() },
                    onLockApp = { viewModel.lockApp() },
                    onUnlockWithPin = { pin -> viewModel.unlockWithPin(pin) },
                    onReplaySplash = {
                        navController.navigate(AppRoutes.SPLASH)
                    }
                )
            }
        }
    }
}
