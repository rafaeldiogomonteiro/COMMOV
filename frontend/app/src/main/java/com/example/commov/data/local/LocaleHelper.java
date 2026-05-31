package com.example.commov.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import java.util.Locale;

public final class LocaleHelper {
    private static final String PREFERENCES_NAME = "commov_settings";
    private static final String KEY_LANGUAGE = "language";
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_PORTUGUESE = "pt";

    private LocaleHelper() {
    }

    public static Context applySavedLocale(Context context) {
        return applyLocale(context, getSavedLanguage(context));
    }

    public static String getSavedLanguage(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return preferences.getString(KEY_LANGUAGE, LANGUAGE_ENGLISH);
    }

    public static void saveLanguage(Context context, String language) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LANGUAGE, language)
                .apply();
    }

    public static void setLanguage(Context context, String language) {
        saveLanguage(context, language);
        applyAppLocale(language);
    }

    public static void applySavedAppLocale(Context context) {
        applyAppLocale(getSavedLanguage(context));
    }

    private static void applyAppLocale(String language) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language));
    }

    public static Context applyLocale(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }
}
