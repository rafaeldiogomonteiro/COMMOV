package com.example.commov.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.commov.R;
import com.example.commov.data.local.LocaleHelper;
import com.example.commov.MainActivity;
import com.example.commov.ui.dashboard.DashboardActivity;
import com.example.commov.viewmodel.LoginUiState;
import com.example.commov.viewmodel.LoginViewModel;

public final class LoginScreen {
    private final Activity activity;
    private final LoginViewModel viewModel = new LoginViewModel();

    private EditText emailInput;
    private EditText passwordInput;
    private TextView emailError;
    private TextView passwordError;
    private boolean renderingState;

    public LoginScreen(Activity activity) {
        this.activity = activity;
    }

    public void bind() {
        bindViews();
        bindEvents();
        viewModel.observe(this::render);
    }

    private void bindViews() {
        emailInput = activity.findViewById(R.id.emailInput);
        passwordInput = activity.findViewById(R.id.passwordInput);
        emailError = activity.findViewById(R.id.emailError);
        passwordError = activity.findViewById(R.id.passwordError);
    }

    private void bindEvents() {
        emailInput.addTextChangedListener(afterTextChanged(viewModel::onEmailChanged));
        passwordInput.addTextChangedListener(afterTextChanged(viewModel::onPasswordChanged));

        activity.findViewById(R.id.languageRow).setOnClickListener(this::showLanguageMenu);
        activity.findViewById(R.id.togglePasswordButton).setOnClickListener(view -> viewModel.onTogglePasswordVisibility());
        activity.findViewById(R.id.loginButton).setOnClickListener(view -> viewModel.onLoginClicked());
        activity.findViewById(R.id.forgotPasswordLink).setOnClickListener(view ->
                Toast.makeText(activity, R.string.login_forgot_password, Toast.LENGTH_SHORT).show()
        );
    }

    private TextWatcher afterTextChanged(TextChangedCallback callback) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!renderingState) {
                    callback.onTextChanged(editable.toString());
                }
            }
        };
    }

    private void render(LoginUiState state) {
        renderingState = true;
        setTextIfChanged(emailInput, state.email);
        setTextIfChanged(passwordInput, state.password);
        setPasswordVisible(state.passwordVisible);
        renderError(emailError, state.emailErrorResId);
        renderError(passwordError, state.passwordErrorResId);
        renderingState = false;

        if (state.loginAccepted) {
            activity.startActivity(new Intent(activity, DashboardActivity.class));
            activity.finish();
        }
    }

    private void setTextIfChanged(EditText editText, String value) {
        if (!editText.getText().toString().equals(value)) {
            editText.setText(value);
            editText.setSelection(editText.getText().length());
        }
    }

    private void setPasswordVisible(boolean visible) {
        TransformationMethod method = visible ? null : PasswordTransformationMethod.getInstance();
        if (passwordInput.getTransformationMethod() != method) {
            passwordInput.setTransformationMethod(method);
            passwordInput.setSelection(passwordInput.getText().length());
        }
    }

    private void renderError(TextView view, int messageResId) {
        if (messageResId == 0) {
            view.setText(null);
            view.setVisibility(View.GONE);
            return;
        }

        view.setText(messageResId);
        view.setVisibility(View.VISIBLE);
    }

    private void showLanguageMenu(View anchor) {
        PopupMenu menu = new PopupMenu(activity, anchor);
        menu.getMenu().add(0, R.id.languageEnglish, 0, R.string.language_english);
        menu.getMenu().add(0, R.id.languagePortuguese, 1, R.string.language_portuguese);
        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.languageEnglish) {
                changeLanguage(LocaleHelper.LANGUAGE_ENGLISH);
                return true;
            }

            if (item.getItemId() == R.id.languagePortuguese) {
                changeLanguage(LocaleHelper.LANGUAGE_PORTUGUESE);
                return true;
            }

            return false;
        });
        menu.show();
    }

    private void changeLanguage(String language) {
        if (LocaleHelper.getSavedLanguage(activity).equals(language)) {
            return;
        }

        LocaleHelper.setLanguage(activity, language);
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    private interface TextChangedCallback {
        void onTextChanged(String value);
    }
}
