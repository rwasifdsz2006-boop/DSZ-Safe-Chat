package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.core.localization.AppLanguage
import com.example.model.SampleChatData
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.DSZSaveChatTheme
import com.example.ui.viewmodel.DashboardTab
import com.example.ui.viewmodel.MessageFilter
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      DSZSaveChatTheme(themeMode = AppThemeMode.DARK) {
        HomeScreen(
          themeMode = AppThemeMode.DARK,
          language = AppLanguage.ENGLISH,
          currentTab = DashboardTab.CHATS,
          messages = SampleChatData.getInitialMessages(),
          mediaList = emptyList(),
          searchQuery = "",
          activeFilter = MessageFilter.ALL,
          selectedMessage = null,
          isDecoyVaultActive = false,
          isOfflineModeEnabled = false,
          primaryPin = "",
          vaultPin = "",
          isPrimaryPinSet = false,
          isVaultPinSet = false,
          isAppLocked = false,
          onSelectTab = {},
          onThemeChange = {},
          onLanguageChange = {},
          onOfflineModeChange = {},
          onSearchChange = {},
          onFilterChange = {},
          onSelectMessage = {},
          onDeleteMessage = {},
          onDeleteMedia = {},
          onSetPrimaryPin = { _, _ -> },
          onRemovePrimaryPin = {},
          onSetVaultPin = { _, _ -> },
          onRemoveVaultPin = {},
          onClearAllChats = {},
          onLockApp = {},
          onUnlockWithPin = { true },
          onReplaySplash = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
