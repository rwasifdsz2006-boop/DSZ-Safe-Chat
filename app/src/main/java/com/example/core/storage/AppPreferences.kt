package com.example.core.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.core.localization.AppLanguage
import com.example.ui.theme.AppThemeMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var themeMode: AppThemeMode
        get() {
            val name = prefs.getString(KEY_THEME_MODE, AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name
            return try {
                AppThemeMode.valueOf(name)
            } catch (_: Exception) {
                AppThemeMode.DARK
            }
        }
        set(value) {
            prefs.edit().putString(KEY_THEME_MODE, value.name).apply()
        }

    var language: AppLanguage
        get() {
            val name = prefs.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.name) ?: AppLanguage.ENGLISH.name
            return try {
                AppLanguage.valueOf(name)
            } catch (_: Exception) {
                AppLanguage.ENGLISH
            }
        }
        set(value) {
            prefs.edit().putString(KEY_LANGUAGE, value.name).apply()
        }

    var isOfflineModeEnabled: Boolean
        get() = prefs.getBoolean(KEY_OFFLINE_MODE, false)
        set(value) {
            prefs.edit().putBoolean(KEY_OFFLINE_MODE, value).apply()
        }

    var isAutoDownloadMedia: Boolean
        get() = prefs.getBoolean(KEY_AUTO_DOWNLOAD, false)
        set(value) {
            prefs.edit().putBoolean(KEY_AUTO_DOWNLOAD, value).apply()
        }

    var primaryPin: String
        get() = prefs.getString(KEY_PRIMARY_PIN, "") ?: ""
        set(value) {
            prefs.edit().putString(KEY_PRIMARY_PIN, value).apply()
        }

    var vaultPin: String
        get() = prefs.getString(KEY_VAULT_PIN, "") ?: ""
        set(value) {
            prefs.edit().putString(KEY_VAULT_PIN, value).apply()
        }

    var isPrimaryLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_PRIMARY_LOCK_ENABLED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_PRIMARY_LOCK_ENABLED, value).apply()
        }

    var isVaultLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_VAULT_LOCK_ENABLED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_VAULT_LOCK_ENABLED, value).apply()
        }

    // --- INDIVIDUAL PER-USER LOCAL AD CAPPING SYSTEM ---
    // Stored 100% locally on each individual user's device SharedPreferences.
    // One user reaching their quota will NEVER affect any other user.

    private fun getTodayDateString(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return formatter.format(Date())
    }

    /**
     * Checks if this specific user/device is eligible to see an Interstitial ad today.
     * Capped strictly to a max of 2 to 3 ads per day per device.
     */
    fun canShowInterstitialAd(): Boolean {
        if (isOfflineModeEnabled) return false
        val today = getTodayDateString()
        val lastDate = prefs.getString(KEY_LOCAL_AD_DATE, "") ?: ""
        
        if (lastDate != today) {
            // New day on this local device: automatically reset local daily counter
            prefs.edit()
                .putString(KEY_LOCAL_AD_DATE, today)
                .putInt(KEY_LOCAL_DAILY_AD_COUNT, 0)
                .apply()
            return true
        }

        val currentCount = prefs.getInt(KEY_LOCAL_DAILY_AD_COUNT, 0)
        return currentCount < MAX_DAILY_INTERSTITIAL_LIMIT
    }

    /**
     * Records a shown Interstitial ad impression locally on this specific device.
     */
    fun recordInterstitialAdImpression() {
        val today = getTodayDateString()
        val lastDate = prefs.getString(KEY_LOCAL_AD_DATE, "") ?: ""
        val currentCount = if (lastDate == today) {
            prefs.getInt(KEY_LOCAL_DAILY_AD_COUNT, 0)
        } else {
            0
        }
        
        prefs.edit()
            .putString(KEY_LOCAL_AD_DATE, today)
            .putInt(KEY_LOCAL_DAILY_AD_COUNT, currentCount + 1)
            .apply()
    }

    /**
     * Returns the remaining daily Interstitial ad quota for this specific device today.
     */
    fun getRemainingDailyInterstitialAds(): Int {
        val today = getTodayDateString()
        val lastDate = prefs.getString(KEY_LOCAL_AD_DATE, "") ?: ""
        val currentCount = if (lastDate == today) {
            prefs.getInt(KEY_LOCAL_DAILY_AD_COUNT, 0)
        } else {
            0
        }
        return (MAX_DAILY_INTERSTITIAL_LIMIT - currentCount).coerceAtLeast(0)
    }

    companion object {
        private const val PREFS_NAME = "dsz_save_chat_preferences"
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_LANGUAGE = "pref_language"
        private const val KEY_OFFLINE_MODE = "pref_offline_mode"
        private const val KEY_AUTO_DOWNLOAD = "pref_auto_download"
        private const val KEY_PRIMARY_PIN = "pref_primary_pin"
        private const val KEY_VAULT_PIN = "pref_vault_pin"
        private const val KEY_PRIMARY_LOCK_ENABLED = "pref_primary_lock_enabled"
        private const val KEY_VAULT_LOCK_ENABLED = "pref_vault_lock_enabled"
        
        // Per-device local ad capping keys (Strictly local to this single device)
        private const val KEY_LOCAL_AD_DATE = "pref_local_ad_date"
        private const val KEY_LOCAL_DAILY_AD_COUNT = "pref_local_daily_ad_count"
        const val MAX_DAILY_INTERSTITIAL_LIMIT = 3 // 2 to 3 ads max per user per day
    }
}
