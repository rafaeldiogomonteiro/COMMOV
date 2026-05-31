package com.example.commov.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.commov.MainActivity;
import com.example.commov.R;
import com.example.commov.data.local.LocaleHelper;
import com.example.commov.viewmodel.SettingsUiState;
import com.example.commov.viewmodel.SettingsViewModel;

public final class SettingsScreen {
    private final Activity activity;
    private final SettingsViewModel viewModel = new SettingsViewModel();

    private TextView currentLanguageText;
    private TextView englishButton;
    private TextView portugueseButton;

    public SettingsScreen(Activity activity) {
        this.activity = activity;
    }

    public void bind() {
        bindViews();
        bindEvents();
        render(viewModel.getState(activity));
    }

    private void bindViews() {
        currentLanguageText = activity.findViewById(R.id.currentLanguageText);
        englishButton = activity.findViewById(R.id.englishLanguageButton);
        portugueseButton = activity.findViewById(R.id.portugueseLanguageButton);
    }

    private void bindEvents() {
        englishButton.setOnClickListener(view -> changeLanguage(LocaleHelper.LANGUAGE_ENGLISH));
        portugueseButton.setOnClickListener(view -> changeLanguage(LocaleHelper.LANGUAGE_PORTUGUESE));
        activity.findViewById(R.id.logoutButton).setOnClickListener(view -> logout());
    }

    private void render(SettingsUiState state) {
        boolean englishSelected = LocaleHelper.LANGUAGE_ENGLISH.equals(state.language);
        int languageNameResId = englishSelected ? R.string.language_english : R.string.language_portuguese;
        currentLanguageText.setText(activity.getString(
                R.string.settings_language_current,
                activity.getString(languageNameResId)
        ));

        renderLanguageButton(englishButton, englishSelected);
        renderLanguageButton(portugueseButton, !englishSelected);
    }

    private void renderLanguageButton(TextView button, boolean selected) {
        button.setBackgroundResource(selected ? R.drawable.bg_language_selected : R.drawable.bg_language_unselected);
        int color = selected ? R.color.bottom_nav_selected : R.color.dashboard_text_primary;
        button.setTextColor(ContextCompat.getColor(activity, color));
    }

    private void changeLanguage(String language) {
        if (viewModel.changeLanguage(activity, language)) {
            Intent intent = new Intent(activity, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            activity.overridePendingTransition(0, 0);
            return;
        }

        render(viewModel.getState(activity));
    }

    private void logout() {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }
}
