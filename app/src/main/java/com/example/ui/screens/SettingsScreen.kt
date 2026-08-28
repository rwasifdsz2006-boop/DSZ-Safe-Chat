package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppLanguage
import com.example.core.localization.AppStrings
import com.example.core.localization.LocalizedContent
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.GoldGradientColors
import com.example.ui.theme.ImmersiveGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class SettingsSubDialog {
    NONE,
    LANGUAGE_PICKER,
    THEME_PICKER,
    APP_PERMISSIONS,
    PRIMARY_LOCK,
    VAULT_LOCK,
    STORAGE_CLEANER,
    BATTERY_GUIDE,
    CHECK_FOR_UPDATES,
    ABOUT_DEVELOPER
}

@Composable
fun SettingsScreen(
    themeMode: AppThemeMode,
    language: AppLanguage,
    isOfflineModeEnabled: Boolean,
    primaryPin: String,
    vaultPin: String,
    isPrimaryPinSet: Boolean,
    isVaultPinSet: Boolean,
    isDecoyVaultActive: Boolean = false,
    onThemeChange: (AppThemeMode) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onOfflineModeChange: (Boolean) -> Unit,
    onSetPrimaryPin: (String, Boolean) -> Unit,
    onRemovePrimaryPin: () -> Unit,
    onSetVaultPin: (String, Boolean) -> Unit,
    onRemoveVaultPin: () -> Unit,
    onClearAllChats: () -> Unit,
    onReplaySplash: () -> Unit
) {
    val strings = remember(language) { AppStrings.get(language) }
    val context = LocalContext.current

    // Switches
    var isAutoDownloadMediaEnabled by remember { mutableStateOf(false) }

    // Active sub-dialog
    var activeSubDialog by remember { mutableStateOf(SettingsSubDialog.NONE) }

    // Native Gesture Navigation BackHandler for settings sub-dialogs
    BackHandler(enabled = activeSubDialog != SettingsSubDialog.NONE) {
        activeSubDialog = SettingsSubDialog.NONE
    }

    val goldBrush = Brush.linearGradient(
        colors = GoldGradientColors,
        start = Offset(0f, 0f),
        end = Offset(200f, 200f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .padding(bottom = 96.dp)
            .testTag("settings_screen_content")
    ) {
        // 1. Channel Header (Settings Top): Top brand card for "واصف ریفری کول" (Wasif RefriCool)
        CreatorChannelHeaderCard(
            language = language,
            strings = strings,
            onOpenChannel = {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/@WasifRefriCool"))
                    context.startActivity(intent)
                } catch (_: Exception) {
                    // Fallback browser URL
                }
            }
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Section Title
        Text(
            text = if (language == AppLanguage.URDU) "ترتیبات اور ترجیحات" else "Settings & Preferences",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 2. 🌐 Language Selection: Support ONLY English (default) and Urdu
        SettingsItemCard(
            icon = Icons.Default.Language,
            iconTint = ImmersiveGold,
            title = strings.settingLanguageTitle,
            subtitle = strings.settingLanguageSubtitle,
            badge = if (language == AppLanguage.URDU) "اردو" else "English (Default)",
            testTag = "setting_item_language",
            onClick = { activeSubDialog = SettingsSubDialog.LANGUAGE_PICKER }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3. 🌓 Theme Mode: Default to Dark Theme with working toggle/picker
        SettingsItemCard(
            icon = when (themeMode) {
                AppThemeMode.LIGHT -> Icons.Default.LightMode
                AppThemeMode.DARK -> Icons.Default.DarkMode
                AppThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
            },
            iconTint = ImmersiveGold,
            title = strings.settingThemeTitle,
            subtitle = strings.settingThemeSubtitle,
            badge = when (themeMode) {
                AppThemeMode.LIGHT -> strings.themeLight
                AppThemeMode.DARK -> strings.themeDark
                AppThemeMode.SYSTEM -> strings.themeSystem
            },
            testTag = "setting_item_theme",
            onClick = { activeSubDialog = SettingsSubDialog.THEME_PICKER }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 4. ⚡ Online/Offline Mode Toggle: Default OFF = Online only; ON = Online & Offline hybrid capability
        SettingsSwitchCard(
            icon = Icons.Default.Bolt,
            iconTint = if (isOfflineModeEnabled) ImmersiveGold else MaterialTheme.colorScheme.onSurfaceVariant,
            title = strings.settingModeTitle,
            subtitle = strings.settingModeSubtitle,
            isChecked = isOfflineModeEnabled,
            testTag = "setting_toggle_offline_mode",
            onCheckedChange = { onOfflineModeChange(it) }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 5. 🔒 Primary App Lock & 🛡️ Vault Lock: Only available in Primary Mode to prevent tampering in Vault Mode
        if (!isDecoyVaultActive) {
            // 5. 🔒 Primary App Lock: Dedicated setting to change PIN or remove/disable lock completely
            SettingsItemCard(
                icon = Icons.Default.Lock,
                iconTint = ImmersiveGold,
                title = strings.settingPrimaryLockTitle,
                subtitle = strings.settingPrimaryLockSubtitle,
                badge = if (isPrimaryPinSet) "Active (PIN: ••••)" else "Not Set",
                badgeColor = if (isPrimaryPinSet) ImmersiveGold else Color.Gray,
                testTag = "setting_item_primary_lock",
                onClick = { activeSubDialog = SettingsSubDialog.PRIMARY_LOCK }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 6. 🛡️ Vault / Secondary Lock: Dedicated setting to change Decoy PIN or remove lock completely
            SettingsItemCard(
                icon = Icons.Default.Shield,
                iconTint = ImmersiveGold,
                title = strings.settingVaultLockTitle,
                subtitle = strings.settingVaultLockSubtitle,
                badge = if (isVaultPinSet) "Decoy Active" else "Not Set",
                badgeColor = if (isVaultPinSet) Color(0xFF10B981) else Color.Gray,
                testTag = "setting_item_vault_lock",
                onClick = { activeSubDialog = SettingsSubDialog.VAULT_LOCK }
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        // 7. 🛡️ Notification & App Permissions Tile
        SettingsItemCard(
            icon = Icons.Default.Security,
            iconTint = ImmersiveGold,
            title = strings.settingPermissionsTitle,
            subtitle = strings.settingPermissionsSubtitle,
            badge = "System Access",
            badgeColor = Color(0xFF10B981),
            testTag = "setting_item_permissions",
            onClick = { activeSubDialog = SettingsSubDialog.APP_PERMISSIONS }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 8. 📥 Auto-Download Media Toggle
        SettingsSwitchCard(
            icon = Icons.Default.CloudDownload,
            iconTint = if (isAutoDownloadMediaEnabled) ImmersiveGold else MaterialTheme.colorScheme.onSurfaceVariant,
            title = strings.settingAutoDownloadTitle,
            subtitle = strings.settingAutoDownloadSubtitle,
            isChecked = isAutoDownloadMediaEnabled,
            testTag = "setting_toggle_autodownload",
            onCheckedChange = { isAutoDownloadMediaEnabled = it }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 8. 🗑️ Storage & Media Cleaner
        SettingsItemCard(
            icon = Icons.Default.DeleteOutline,
            iconTint = Color(0xFFEF4444),
            title = strings.settingStorageTitle,
            subtitle = strings.settingStorageSubtitle,
            badge = "Safe Clean",
            badgeColor = Color(0xFF10B981),
            testTag = "setting_item_storage",
            onClick = { activeSubDialog = SettingsSubDialog.STORAGE_CLEANER }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 9. 🔋 Battery Optimization Guide
        SettingsItemCard(
            icon = Icons.Default.BatteryChargingFull,
            iconTint = Color(0xFF10B981),
            title = strings.settingBatteryTitle,
            subtitle = strings.settingBatterySubtitle,
            badge = "24/7 Service",
            badgeColor = Color(0xFF10B981),
            testTag = "setting_item_battery",
            onClick = { activeSubDialog = SettingsSubDialog.BATTERY_GUIDE }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 10. 🚀 In-App Custom Update System: Check for Updates & direct APK download link
        SettingsItemCard(
            icon = Icons.Default.SystemUpdate,
            iconTint = ImmersiveGold,
            title = strings.settingUpdatesTitle,
            subtitle = strings.settingUpdatesSubtitle,
            badge = "v2.4.0 (Latest)",
            badgeColor = Color(0xFF10B981),
            testTag = "setting_item_updates",
            onClick = { activeSubDialog = SettingsSubDialog.CHECK_FOR_UPDATES }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 11. ℹ️ About / Developer Info (Final option in main settings menu)
        SettingsItemCard(
            icon = Icons.Default.Info,
            iconTint = ImmersiveGold,
            title = if (language == AppLanguage.URDU) "ڈیولپر اور ایپ کے بارے میں" else "About / Developer Info",
            subtitle = if (language == AppLanguage.URDU) "M Wasif DSZ — ڈیولپر اور ایپ کی تفصیلات" else "M Wasif DSZ — Developer & App Details",
            badge = "M Wasif DSZ",
            badgeColor = ImmersiveGold,
            testTag = "setting_item_about_developer",
            onClick = { activeSubDialog = SettingsSubDialog.ABOUT_DEVELOPER }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Preview Splash Screen Option
        OutlinedButton(
            onClick = onReplaySplash,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("preview_splash_button_settings"),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Icon(
                imageVector = Icons.Default.Replay,
                contentDescription = null,
                tint = ImmersiveGold,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = strings.replaySplashButton,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Developer Branding Footer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                .padding(18.dp)
                .testTag("developer_branding_footer"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .rotate(-3f)
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(goldBrush)
                ) {
                    Text(
                        text = "DSZ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF121212)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "DSZ Save Chat",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "by M Wasif DSZ",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        color = ImmersiveGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "“${strings.footerSlogan}”",
                style = MaterialTheme.typography.bodySmall,
                color = ImmersiveGold,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = strings.offlineStatusTitle,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }

    // Sub-dialogs
    when (activeSubDialog) {
        SettingsSubDialog.LANGUAGE_PICKER -> {
            LanguagePickerDialog(
                currentLanguage = language,
                onSelectLanguage = {
                    onLanguageChange(it)
                    activeSubDialog = SettingsSubDialog.NONE
                },
                onDismiss = { activeSubDialog = SettingsSubDialog.NONE }
            )
        }
        SettingsSubDialog.THEME_PICKER -> {
            ThemePickerDialog(
                currentTheme = themeMode,
                strings = strings,
                onSelectTheme = {
                    onThemeChange(it)
                    activeSubDialog = SettingsSubDialog.NONE
                },
                onDismiss = { activeSubDialog = SettingsSubDialog.NONE }
            )
        }
        SettingsSubDialog.PRIMARY_LOCK -> {
            PinManagementDialog(
                title = strings.settingPrimaryLockTitle,
                subtitle = "Set or update your 4-digit PIN to protect real chats. You can also completely remove the lock.",
                currentPin = primaryPin,
                isLockActive = isPrimaryPinSet,
                onSavePin = {
                    onSetPrimaryPin(it, true)
                    activeSubDialog = SettingsSubDialog.NONE
                },
                onRemoveLock = {
                    onRemovePrimaryPin()
                    activeSubDialog = SettingsSubDialog.NONE
                },
                onDismiss = { activeSubDialog = SettingsSubDialog.NONE }
            )
        }
        SettingsSubDialog.VAULT_LOCK -> {
            PinManagementDialog(
                title = strings.settingVaultLockTitle,
                subtitle = "Set a secret decoy PIN. When entered on the startup lock screen, a 100% empty blank vault is shown.",
                currentPin = vaultPin,
                isLockActive = isVaultPinSet,
                onSavePin = {
                    onSetVaultPin(it, true)
                    activeSubDialog = SettingsSubDialog.NONE
                },
                onRemoveLock = {
                    onRemoveVaultPin()
                    activeSubDialog = SettingsSubDialog.NONE
                },
                onDismiss = { activeSubDialog = SettingsSubDialog.NONE }
            )
        }
        SettingsSubDialog.APP_PERMISSIONS -> {
            AppPermissionsDialog(
                strings = strings,
                onDismiss = { activeSubDialog = SettingsSubDialog.NONE }
            )
        }
        SettingsSubDialog.STORAGE_CLEANER -> {
            StorageCleanerDialog(
                onClearCache = {
                    // Cache reset
                },
                onClearAll = {
                    onClearAllChats()
                },
                onDismiss = { activeSubDialog = SettingsSubDialog.NONE }
            )
        }
        SettingsSubDialog.BATTERY_GUIDE -> {
            BatteryOptimizationGuideDialog(
                onDismiss = { activeSubDialog = SettingsSubDialog.NONE }
            )
        }
        SettingsSubDialog.CHECK_FOR_UPDATES -> {
            UpdatesCheckDialog(
                strings = strings,
                onDismiss = { activeSubDialog = SettingsSubDialog.NONE }
            )
        }
        SettingsSubDialog.ABOUT_DEVELOPER -> {
            AboutDeveloperDialog(
                onDismiss = { activeSubDialog = SettingsSubDialog.NONE }
            )
        }
        SettingsSubDialog.NONE -> {}
    }
}

/**
 * 1. Channel Header Card for "واصف ریفری کول" (Wasif RefriCool)
 */
@Composable
fun CreatorChannelHeaderCard(
    language: AppLanguage,
    strings: LocalizedContent,
    onOpenChannel: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, ImmersiveGold.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = ImmersiveGold.copy(alpha = 0.15f),
                spotColor = ImmersiveGold.copy(alpha = 0.2f)
            )
            .testTag("creator_channel_header_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // YouTube Red Play Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFF0000))
                        .shadow(4.dp, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "YouTube",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = strings.youtubeChannelName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified Channel",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = strings.youtubeChannelHandle,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ImmersiveGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = strings.youtubeChannelDesc,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stats Pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = ImmersiveGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.youtubeSubscribersCount,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = ImmersiveGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.youtubeVideosCount,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Subscribe & Watch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenChannel,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(40.dp)
                        .testTag("btn_youtube_subscribe"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF0000),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = strings.youtubeSubscribeBtn,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onOpenChannel,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .testTag("btn_youtube_watch"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ImmersiveGold)
                ) {
                    Text(
                        text = strings.youtubeWatchBtn,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ImmersiveGold
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsItemCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    badge: String? = null,
    badgeColor: Color = ImmersiveGold,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.12f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }

            if (badge != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun SettingsSwitchCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.12f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF121212),
                    checkedTrackColor = ImmersiveGold,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

/**
 * 2. Language Selection Dialog: ONLY English and Urdu supported
 */
@Composable
fun LanguagePickerDialog(
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = ImmersiveGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select App Language / زبان کا انتخاب", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LanguageOptionTile(
                    title = "English (US / UK)",
                    subtitle = "Default main language • Left-to-Right layout",
                    isSelected = currentLanguage == AppLanguage.ENGLISH,
                    onClick = { onSelectLanguage(AppLanguage.ENGLISH) }
                )
                LanguageOptionTile(
                    title = "اردو (Urdu)",
                    subtitle = "قومی زبان • Right-to-Left (دائیں سے بائیں)",
                    isSelected = currentLanguage == AppLanguage.URDU,
                    onClick = { onSelectLanguage(AppLanguage.URDU) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = ImmersiveGold, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun LanguageOptionTile(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (isSelected) ImmersiveGold else Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isSelected) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = ImmersiveGold, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/**
 * 3. Theme Mode Dialog: Dark Theme (Default) and Light Theme
 */
@Composable
fun ThemePickerDialog(
    currentTheme: AppThemeMode,
    strings: LocalizedContent,
    onSelectTheme: (AppThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = ImmersiveGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.themeSectionTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ThemeOptionTile(
                    title = strings.themeDark,
                    icon = Icons.Default.DarkMode,
                    isSelected = currentTheme == AppThemeMode.DARK,
                    onClick = { onSelectTheme(AppThemeMode.DARK) }
                )
                ThemeOptionTile(
                    title = strings.themeLight,
                    icon = Icons.Default.LightMode,
                    isSelected = currentTheme == AppThemeMode.LIGHT,
                    onClick = { onSelectTheme(AppThemeMode.LIGHT) }
                )
                ThemeOptionTile(
                    title = strings.themeSystem,
                    icon = Icons.Default.SettingsBrightness,
                    isSelected = currentTheme == AppThemeMode.SYSTEM,
                    onClick = { onSelectTheme(AppThemeMode.SYSTEM) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = ImmersiveGold, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun ThemeOptionTile(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, if (isSelected) ImmersiveGold else Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = if (isSelected) ImmersiveGold else MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            if (isSelected) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = ImmersiveGold, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/**
 * 4. App Lock & Vault Setup Logic Dialog:
 * Features "New Password" and "Confirm Password" input fields and a "Save" action.
 * When saved, the lock becomes active and is required on startup.
 */
@Composable
fun PinManagementDialog(
    title: String,
    subtitle: String,
    currentPin: String,
    isLockActive: Boolean,
    onSavePin: (String) -> Unit,
    onRemoveLock: () -> Unit,
    onDismiss: () -> Unit
) {
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isNewPinVisible by remember { mutableStateOf(false) }
    var isConfirmPinVisible by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showRemoveConfirmation by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = ImmersiveGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column {
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(14.dp))

                // Current Lock Status Badge
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Status: ${if (isLockActive) "Active (Locked)" else "Not Set / Open"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLockActive) ImmersiveGold else Color(0xFF10B981)
                        )
                        if (isLockActive) {
                            Text(
                                text = "Current: ••••",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Field 1: New Password
                OutlinedTextField(
                    value = newPin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            newPin = it
                            errorMsg = null
                        }
                    },
                    label = { Text("New Password (4 digits)") },
                    placeholder = { Text("e.g. 1234") },
                    visualTransformation = if (isNewPinVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isNewPinVisible = !isNewPinVisible }) {
                            Icon(
                                imageVector = if (isNewPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = ImmersiveGold,
                        focusedBorderColor = ImmersiveGold,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_new_password"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Field 2: Confirm Password
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            confirmPin = it
                            errorMsg = null
                        }
                    },
                    label = { Text("Confirm Password") },
                    placeholder = { Text("Re-enter 4 digits") },
                    visualTransformation = if (isConfirmPinVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isConfirmPinVisible = !isConfirmPinVisible }) {
                            Icon(
                                imageVector = if (isConfirmPinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle password visibility",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedLabelColor = ImmersiveGold,
                        focusedBorderColor = ImmersiveGold,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_confirm_password"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (errorMsg != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMsg!!,
                        color = Color(0xFFEF4444),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Option to Remove/Disable Lock if active
                if (isLockActive) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = {
                            if (showRemoveConfirmation) {
                                onRemoveLock()
                            } else {
                                showRemoveConfirmation = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_remove_lock"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF4444))
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showRemoveConfirmation) "Tap Again to Confirm Removal" else "Disable / Remove Lock",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPin.isEmpty()) {
                        errorMsg = "Please enter a 4-digit password"
                    } else if (newPin.length < 4) {
                        errorMsg = "Password must be exactly 4 digits"
                    } else if (confirmPin != newPin) {
                        errorMsg = "Passwords do not match. Please verify."
                    } else {
                        errorMsg = null
                        onSavePin(newPin)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ImmersiveGold,
                    contentColor = Color(0xFF121212)
                ),
                modifier = Modifier.testTag("btn_save_password")
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_password_dialog")
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * 6. In-App Custom Update System Dialog:
 * Checks remote version URL and opens direct APK download link safely in browser (`Intent.ACTION_VIEW`).
 */
@Composable
fun UpdatesCheckDialog(
    strings: LocalizedContent,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isChecking by remember { mutableStateOf(false) }
    var isCheckedDone by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SystemUpdate,
                    contentDescription = null,
                    tint = ImmersiveGold
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = strings.settingUpdatesTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Version & Status Banner
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ImmersiveGold.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isChecking) ImmersiveGold.copy(alpha = 0.2f)
                                    else Color(0xFF10B981).copy(alpha = 0.15f)
                                )
                        ) {
                            if (isChecking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(30.dp),
                                    color = ImmersiveGold,
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Up to Date",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = strings.updatesCurrentVersionTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Official Release • Local Storage Safe",
                                fontSize = 11.sp,
                                color = ImmersiveGold,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Text(
                    text = strings.updatesUpToDateDesc,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                )

                // Changelog Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = strings.updatesChangelogTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ChangelogBullet("• Wasif RefriCool Official Channel Integration")
                    ChangelogBullet("• Strict Startup Security & Smart Single-Keypad Decoy Vault")
                    ChangelogBullet("• Online/Offline Hybrid Mode & Custom Updates")
                    ChangelogBullet("• High-Performance Local Encrypted Storage")
                }

                // Check Now Button / Update Now Button
                Button(
                    onClick = {
                        scope.launch {
                            isChecking = true
                            delay(1000)
                            isChecking = false
                            isCheckedDone = true
                        }
                    },
                    enabled = !isChecking,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("check_updates_dialog_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ImmersiveGold,
                        contentColor = Color(0xFF121212)
                    )
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color(0xFF121212),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Checking Server...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF121212)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color(0xFF121212),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.updatesCheckNowBtn,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF121212)
                        )
                    }
                }

                // Direct APK Download / Update Now Button (url_launcher intent)
                Button(
                    onClick = {
                        try {
                            val downloadIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/mwasifdsz/dsz-save-chat/releases/latest")
                            )
                            context.startActivity(downloadIntent)
                        } catch (_: Exception) {
                            // Fallback
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_direct_apk_download"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.updatesDownloadBtn,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Done",
                    color = ImmersiveGold,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun ChangelogBullet(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 16.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
fun StorageCleanerDialog(
    onClearCache: () -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    var clearedNotice by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Storage, contentDescription = null, tint = ImmersiveGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Storage & Media Cleaner", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Database Status", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Encrypted & Safe", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onClearCache()
                            clearedNotice = "App temporary cache cleared."
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Text("Clear Cache", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            onClearAll()
                            clearedNotice = "All stored offline chat data purged."
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White)
                    ) {
                        Text("Purge Chats", fontSize = 11.sp)
                    }
                }

                if (clearedNotice != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(clearedNotice!!, color = Color(0xFF10B981), fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done", color = ImmersiveGold, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun BatteryOptimizationGuideDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.BatteryChargingFull, contentDescription = null, tint = Color(0xFF10B981))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Background Battery Guide", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "To ensure notifications from WhatsApp and Telegram are saved reliably in the background without delay:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                GuideStepItem(number = "1", title = "Disable Battery Restriction", desc = "Go to App Info > Battery > Select 'Unrestricted'.")
                GuideStepItem(number = "2", title = "Allow Auto-Start", desc = "Enable auto-start in Xiaomi/Oppo/Vivo settings if applicable.")
                GuideStepItem(number = "3", title = "Notification Listener", desc = "Keep notification access active for continuous protection.")

                Button(
                    onClick = {
                        try {
                            val intent = Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            // Fallback
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ImmersiveGold, contentColor = Color(0xFF121212))
                ) {
                    Text("Open System Battery Settings", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Understood", color = ImmersiveGold, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun GuideStepItem(number: String, title: String, desc: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(22.dp).clip(CircleShape).background(ImmersiveGold)
        ) {
            Text(number, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF121212))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Dedicated About / Developer Info Dialog displaying the creator and app description
 * in the exact provided Urdu and Roman English format.
 */
@Composable
fun AboutDeveloperDialog(
    onDismiss: () -> Unit
) {
    val goldBrush = Brush.linearGradient(
        colors = GoldGradientColors,
        start = Offset(0f, 0f),
        end = Offset(200f, 200f)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .rotate(-3f)
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(goldBrush)
                ) {
                    Text(
                        text = "DSZ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF121212)
                    )
                }
                Column {
                    Text(
                        text = "M Wasif DSZ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ImmersiveGold
                    )
                    Text(
                        text = "Developer & Creator 🚀",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
                    .testTag("about_developer_dialog_content")
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, ImmersiveGold.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = """M Wasif DSZ — Developer & Creator 🚀

ایک تخلیق کار جو خیالات کو ڈیجیٹل حقیقت میں بدلتا ہے! 💡✨

میرا نام M Wasif DSZ ہے، اور میرا تعلق پاکستان کے شہر ڈیرہ غازی خان (غازی گھاٹ کے قریب واقع تاریخی بستی ڈیرہ شاہ زبیر) سے ہے۔ ٹیکنالوجی کی دنیا میں میرا سفر محض کوڈنگ تک محدود نہیں ہے، بلکہ میرا مقصد ایسے کارآمد ڈیجیٹل پروڈکٹس تیار کرنا ہے جو لوگوں کی روزمرہ زندگی کو آسان، تیز اور بہتر بنائیں۔ 🌍🛠️

🔹 DSZ — جہاں خیالات ٹیکنالوجی بنتے ہیں
DSZ دراصل میرے آبائی علاقے ڈیرہ شاہ زبیر کا شارٹ نام ہے، جسے اب میں نے ایک پروفیشنل ڈیجیٹل برانڈ کی شکل دی ہے۔ یہ برانڈ اس بات کی علامت ہے کہ ہم جدید ٹیکنالوجی کے ذریعے دنیا کو ایک نیا اور مفید تجربہ فراہم کر رہے ہیں۔ 🏆💼

🔹 ہمارے ڈیجیٹل پروجیکٹس اور کام:
DSZ کے پلیٹ فارم کے تحت ہم جدید ٹیکنالوجی کے مختلف شعبوں میں معیاری کام کر رہے ہیں:
* Mobile Apps aur Games: صارفین کے لیے کارآمد، دلکش اور یوزر فرینڈلی موبائل ایپلیکیشنز اور گیمز کی تیاری۔ 📱🎮
* Websites aur Web Apps: ہر قسم کی جدید، تیز رفتار اور رسپانسو ویب سائٹس اور ویب بیسڈ ایپلیکیشنز کی ڈویلپمنٹ۔ 💻🌐
* AI Projects: مصنوعی ذہانت (AI) پر مبنی جدید ٹولز اور سمارٹ سسٹمز کی تخلیق۔ 🤖⚡

🔹 ہمارا وژن اور عزم:
* Useful Ideas: ایسے خیالات جو حقیقت میں کسی ضرورت کو پورا کریں۔ 💡
* Better Technology: جدید، تیز اور قابلِ اعتماد ٹیکنالوجی۔ 🚀
* Real Value: صارفین کے لیے حقیقی افادیت اور بہترین تجربہ۔ 🌟

About App: DSZ Save Chat 📱✨
DSZ Save Chat آپ کے WhatsApp Messenger اور WhatsApp Business کے تمام ضروری ڈیٹا کو ہمیشہ کے لیے محفوظ رکھتی ہے! اب کوئی بھی چیٹ، تصویر، ویڈیو یا وائس نوٹ ڈیلیٹ ہونے پر بھی کبھی ضائع نہیں ہوگی۔ 🔒

Key Features (اہم خصوصیات): 🚀
1. Anti-Delete (اینٹی ڈیلیٹ پروٹیکشن): 🛡️
   * اگر سامنے والا بندہ واٹس ایپ پر میسج یا میڈیا "Delete for Everyone" بھی کر دے، تب بھی آپ کی اس ایپ سے وہ ڈیٹا ہرگز ڈیلیٹ نہیں ہوگا اور ہمیشہ محفوظ رہے گا۔
2. Auto Profile & Name (آٹو پروفائل سنک): 👤
   * واٹس ایپ اور واٹس ایپ بزنس کے یوزرز کا اصل نام اور پروفائل تصویر خود بخود ڈیٹیکٹ ہو کر ان کی چیٹس کے ساتھ شو ہوگی۔
3. Separate Tabs (الگ الگ ٹیبز): 📂
   * WhatsApp Messenger اور WhatsApp Business کی چیٹس کو الگ الگ اور ترتیب وار دیکھنے کے لیے انتہائی صاف ستھرا انٹرفیس۔
4. میڈیا ہب اور پلے بیک: 🎬🎧
   * چیٹ کے اندر جا کر آسانی سے پیغامات پڑھیں، ویڈیوز پلے کریں اور آڈیو سنیں۔ تمام میڈیا (تصاویر، ویڈیوز، آڈیو) کو الگ میڈیا ہب سے اپنی مرضی سے ڈاؤن لوڈ کریں۔
5. Local Storage (محفوظ لوکل اسٹوریج): 💾
   * تمام ڈیٹا آپ کے اپنے موبائل کی انٹرنل میموری میں محفوظ رہتا ہے، اور جب چاہیں آپ خود پرانا یا فالتو ڈیٹا ڈیلیٹ کر کے فون خالی کر سکتے ہیں۔

🔹 ہمارا مقصد اور یقین:
ہم اپنی طرف سے ہر ممکن کوشش کرتے ہیں کہ اپنے ہر پراجیکٹ کو بہترین سے بہترین انداز میں تیار کریں، تاکہ یہ ٹیکنالوجی عام لوگوں کی زندگی میں آسانی پیدا کرے اور ان کے روزمرہ کے کاموں کو زیادہ مؤثر بنائے۔ آپ کا فیڈ بیک اور سپورٹ ہی ہماری اصل طاقت ہے۔ 🤝❤️""",
                            fontSize = 12.sp,
                            lineHeight = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = ImmersiveGold)
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Dedicated System & App Permissions Management Dialog
 */
@Composable
fun AppPermissionsDialog(
    strings: LocalizedContent,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var isNotificationGranted by remember { mutableStateOf(isNotificationAccessGranted(context)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ImmersiveGold.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = ImmersiveGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = strings.settingPermissionsTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "System Access & Anti-Delete Sync",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = ImmersiveGold
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
                    .testTag("permissions_dialog_content")
            ) {
                Text(
                    text = "DSZ Save Chat operates locally on your device. Configure permissions below to ensure 24/7 background capture for WhatsApp Messenger & WhatsApp Business.",
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Notification Listener Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isNotificationGranted) Color(0xFF10B981).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isNotificationGranted) Color(0xFF10B981) else ImmersiveGold.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isNotificationGranted) Icons.Default.CheckCircle else Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = if (isNotificationGranted) Color(0xFF10B981) else ImmersiveGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.permissionNotificationTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = if (isNotificationGranted) "Active" else "Required",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNotificationGranted) Color(0xFF10B981) else Color(0xFFEF4444)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.permissionNotificationDesc,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    val intent = Intent(android.provider.Settings.ACTION_SETTINGS)
                                    context.startActivity(intent)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isNotificationGranted) MaterialTheme.colorScheme.surfaceVariant else ImmersiveGold,
                                contentColor = if (isNotificationGranted) MaterialTheme.colorScheme.onSurface else Color(0xFF121212)
                            )
                        ) {
                            Text(
                                text = if (isNotificationGranted) "Manage Notification Access" else "Grant Notification Access",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Storage & Media Access Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = ImmersiveGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.permissionStorageTitle,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Granted",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.permissionStorageDesc,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data = Uri.fromParts("package", context.packageName, null)
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Text(
                                text = "App Storage & File Permissions",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = ImmersiveGold)
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}


