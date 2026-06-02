package com.example.commov.viewmodel

import android.content.Context
import com.example.commov.data.local.LocaleHelper

class SettingsViewModel {
    fun getState(context: Context): SettingsUiState {
        return SettingsUiState(LocaleHelper.getSavedLanguage(context))
    }

    fun changeLanguage(context: Context, language: String): Boolean {
        if (LocaleHelper.getSavedLanguage(context) == language) {
            return false
        }

        LocaleHelper.setLanguage(context, language)
        return true
    }
}
