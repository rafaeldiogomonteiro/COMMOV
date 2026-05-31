package com.example.commov.viewmodel;

import android.text.TextUtils;
import android.util.Patterns;

import androidx.annotation.StringRes;

import com.example.commov.R;
import com.example.commov.model.LoginModel;

public final class LoginViewModel {
    public interface StateObserver {
        void onStateChanged(LoginUiState state);
    }

    private LoginModel model = new LoginModel("ricardo@projectflow.com", "password", false);
    private StateObserver observer;

    public void observe(StateObserver observer) {
        this.observer = observer;
        publish(0, 0, false);
    }

    public void onEmailChanged(String email) {
        model = model.withEmail(email);
        publish(0, 0, false);
    }

    public void onPasswordChanged(String password) {
        model = model.withPassword(password);
        publish(0, 0, false);
    }

    public void onTogglePasswordVisibility() {
        model = model.withPasswordVisible(!model.isPasswordVisible());
        publish(0, 0, false);
    }

    public void onLoginClicked() {
        int emailError = 0;
        int passwordError = 0;

        if (!Patterns.EMAIL_ADDRESS.matcher(model.getEmail()).matches()) {
            emailError = R.string.login_error_email;
        }

        if (TextUtils.isEmpty(model.getPassword()) || model.getPassword().length() < 6) {
            passwordError = R.string.login_error_password;
        }

        publish(emailError, passwordError, emailError == 0 && passwordError == 0);
    }

    private void publish(@StringRes int emailError, @StringRes int passwordError, boolean loginAccepted) {
        if (observer == null) {
            return;
        }

        observer.onStateChanged(new LoginUiState(
                model.getEmail(),
                model.getPassword(),
                model.isPasswordVisible(),
                emailError,
                passwordError,
                loginAccepted
        ));
    }
}
