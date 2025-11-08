package com.example.minandroidapp.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {
    private const val PREFS = "quick_log_prefs"
    private const val KEY_THEME = "theme_mode"

    enum class ThemeOption(val delegateMode: Int) {
        SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
        LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
        DARK(AppCompatDelegate.MODE_NIGHT_YES),
    }

    fun applySavedTheme(context: Context) {
        val option = getSavedOption(context)
        AppCompatDelegate.setDefaultNightMode(option.delegateMode)
    }

    fun updateTheme(context: Context, option: ThemeOption) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, option.name)
            .apply()
        AppCompatDelegate.setDefaultNightMode(option.delegateMode)
    }

    fun getSavedOption(context: Context): ThemeOption {
        val value = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_THEME, ThemeOption.SYSTEM.name)
        return runCatching { ThemeOption.valueOf(value ?: ThemeOption.SYSTEM.name) }
            .getOrDefault(ThemeOption.SYSTEM)
    }
}
