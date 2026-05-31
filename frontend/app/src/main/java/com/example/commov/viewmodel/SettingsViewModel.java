package com.example.commov.viewmodel;

import android.content.Context;

import com.example.commov.data.local.LocaleHelper;

public final class SettingsViewModel {
    public SettingsUiState getState(Context context) {
        return new SettingsUiState(LocaleHelper.getSavedLanguage(context));
    }

    public boolean changeLanguage(Context context, String language) {
        if (LocaleHelper.getSavedLanguage(context).equals(language)) {
            return false;
        }

        LocaleHelper.setLanguage(context, language);
        return true;
    }
}
