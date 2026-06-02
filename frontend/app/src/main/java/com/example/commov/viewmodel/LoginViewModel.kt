package com.example.commov.viewmodel

import android.text.TextUtils
import android.util.Patterns
import androidx.annotation.StringRes
import com.example.commov.R
import com.example.commov.model.LoginModel

class LoginViewModel {
    fun interface StateObserver {
        fun onStateChanged(state: LoginUiState)
    }

    private var model = LoginModel("ricardo@projectflow.com", "password", false)
    private var observer: StateObserver? = null

    fun observe(observer: StateObserver) {
        this.observer = observer
        publish(0, 0, false)
    }

    fun onEmailChanged(email: String) {
        model = model.withEmail(email)
        publish(0, 0, false)
    }

    fun onPasswordChanged(password: String) {
        model = model.withPassword(password)
        publish(0, 0, false)
    }

    fun onTogglePasswordVisibility() {
        model = model.withPasswordVisible(!model.passwordVisible)
        publish(0, 0, false)
    }

    fun onLoginClicked() {
        var emailError = 0
        var passwordError = 0

        if (!Patterns.EMAIL_ADDRESS.matcher(model.email).matches()) {
            emailError = R.string.login_error_email
        }

        if (TextUtils.isEmpty(model.password) || model.password.length < 6) {
            passwordError = R.string.login_error_password
        }

        publish(emailError, passwordError, emailError == 0 && passwordError == 0)
    }

    private fun publish(
        @StringRes emailError: Int,
        @StringRes passwordError: Int,
        loginAccepted: Boolean
    ) {
        observer?.onStateChanged(
            LoginUiState(
                email = model.email,
                password = model.password,
                passwordVisible = model.passwordVisible,
                emailErrorResId = emailError,
                passwordErrorResId = passwordError,
                loginAccepted = loginAccepted
            )
        )
    }
}
