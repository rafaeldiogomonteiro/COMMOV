package com.example.commov.data.local

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {
    private const val PREFERENCES_NAME = "commov_settings"
    private const val KEY_LANGUAGE = "language"
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_PORTUGUESE = "pt"

    fun applySavedLocale(context: Context): Context {
        return applyLocale(context, getSavedLanguage(context))
    }

    fun getSavedLanguage(context: Context): String {
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        return preferences.getString(KEY_LANGUAGE, LANGUAGE_ENGLISH) ?: LANGUAGE_ENGLISH
    }

    fun saveLanguage(context: Context, language: String) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language)
            .apply()
    }

    fun setLanguage(context: Context, language: String) {
        saveLanguage(context, language)
        applyAppLocale(language)
    }

    fun applySavedAppLocale(context: Context) {
        applyAppLocale(getSavedLanguage(context))
    }

    private fun applyAppLocale(language: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
    }

    fun applyLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }
}
