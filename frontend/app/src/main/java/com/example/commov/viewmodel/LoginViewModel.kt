package com.example.commov.viewmodel

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Patterns
import androidx.annotation.StringRes
import com.example.commov.R
import com.example.commov.data.local.SessionManager
import com.example.commov.data.remote.AuthApi
import com.example.commov.data.remote.LoginResult
import com.example.commov.model.LoginModel

class LoginViewModel(
    context: Context,
    private val authApi: AuthApi = AuthApi(),
    private val sessionManager: SessionManager = SessionManager(context.applicationContext)
) {
    fun interface StateObserver {
        fun onStateChanged(state: LoginUiState)
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private var model = LoginModel("admin@commov.local", "admin123", false)
    private var observer: StateObserver? = null
    private var isLoading = false

    fun observe(observer: StateObserver) {
        this.observer = observer
        publish(0, 0, 0, false)
    }

    fun onEmailChanged(email: String) {
        model = model.withEmail(email)
        publish(0, 0, 0, false)
    }

    fun onPasswordChanged(password: String) {
        model = model.withPassword(password)
        publish(0, 0, 0, false)
    }

    fun onTogglePasswordVisibility() {
        model = model.withPasswordVisible(!model.passwordVisible)
        publish(0, 0, 0, false)
    }

    fun onLoginClicked() {
        if (isLoading) {
            return
        }

        var emailError = 0
        var passwordError = 0

        if (!Patterns.EMAIL_ADDRESS.matcher(model.email).matches()) {
            emailError = R.string.login_error_email
        }

        if (TextUtils.isEmpty(model.password) || model.password.length < 6) {
            passwordError = R.string.login_error_password
        }

        if (emailError != 0 || passwordError != 0) {
            publish(emailError, passwordError, 0, false)
            return
        }

        isLoading = true
        publish(0, 0, 0, false)

        val email = model.email.trim()
        val password = model.password

        Thread {
            val result = authApi.login(email, password)
            mainHandler.post {
                isLoading = false
                when (result) {
                    is LoginResult.Success -> {
                        sessionManager.saveSession(result.token, result.user)
                        publish(0, 0, 0, true)
                    }
                    LoginResult.InvalidCredentials -> {
                        publish(0, 0, R.string.login_error_invalid_credentials, false)
                    }
                    LoginResult.NetworkError -> {
                        publish(0, 0, R.string.login_error_network, false)
                    }
                    is LoginResult.ServerError -> {
                        publish(0, 0, R.string.login_error_unknown, false)
                    }
                }
            }
        }.start()
    }

    private fun publish(
        @StringRes emailError: Int,
        @StringRes passwordError: Int,
        @StringRes generalError: Int,
        loginAccepted: Boolean
    ) {
        observer?.onStateChanged(
            LoginUiState(
                email = model.email,
                password = model.password,
                passwordVisible = model.passwordVisible,
                emailErrorResId = emailError,
                passwordErrorResId = passwordError,
                generalErrorResId = generalError,
                isLoading = isLoading,
                loginAccepted = loginAccepted
            )
        )
    }
}
