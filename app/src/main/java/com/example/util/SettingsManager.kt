package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppPreferences(
    val colorIndex: Int = 0,
    val themeMode: String = "System default", // "System default", "Light", "Dark"
    val beep: Boolean = true,
    val vibrate: Boolean = true,
    val copyToClipboard: Boolean = false,
    val urlInfo: Boolean = true,
    val batchScanMode: Boolean = false
)

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("qr_app_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppPreferences> = _settings.asStateFlow()

    private fun loadSettings(): AppPreferences {
        return AppPreferences(
            colorIndex = prefs.getInt("color_index", 0),
            themeMode = prefs.getString("theme_mode", "System default") ?: "System default",
            beep = prefs.getBoolean("beep", true),
            vibrate = prefs.getBoolean("vibrate", true),
            copyToClipboard = prefs.getBoolean("copy_to_clipboard", false),
            urlInfo = prefs.getBoolean("url_info", true),
            batchScanMode = prefs.getBoolean("batch_scan_mode", false)
        )
    }

    fun setColorIndex(index: Int) {
        prefs.edit().putInt("color_index", index).apply()
        _settings.value = _settings.value.copy(colorIndex = index)
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
        _settings.value = _settings.value.copy(themeMode = mode)
    }

    fun setBeep(enabled: Boolean) {
        prefs.edit().putBoolean("beep", enabled).apply()
        _settings.value = _settings.value.copy(beep = enabled)
    }

    fun setVibrate(enabled: Boolean) {
        prefs.edit().putBoolean("vibrate", enabled).apply()
        _settings.value = _settings.value.copy(vibrate = enabled)
    }

    fun setCopyToClipboard(enabled: Boolean) {
        prefs.edit().putBoolean("copy_to_clipboard", enabled).apply()
        _settings.value = _settings.value.copy(copyToClipboard = enabled)
    }

    fun setUrlInfo(enabled: Boolean) {
        prefs.edit().putBoolean("url_info", enabled).apply()
        _settings.value = _settings.value.copy(urlInfo = enabled)
    }

    fun setBatchScanMode(enabled: Boolean) {
        prefs.edit().putBoolean("batch_scan_mode", enabled).apply()
        _settings.value = _settings.value.copy(batchScanMode = enabled)
    }
}
