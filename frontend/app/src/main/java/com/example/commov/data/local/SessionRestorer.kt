package com.example.commov.data.local

import com.example.commov.data.remote.AuthApi
import com.example.commov.data.remote.CheckLoginResult

object SessionRestorer {
    sealed interface Result {
        data object Valid : Result
        data object NeedsLogin : Result
        data object OfflineValid : Result
    }

    fun validate(
        sessionManager: SessionManager,
        authApi: AuthApi = AuthApi()
    ): Result {
        val token = sessionManager.token()
        if (token.isNullOrBlank() || sessionManager.currentUser() == null) {
            return Result.NeedsLogin
        }

        return when (val result = authApi.checkLogin(token)) {
            is CheckLoginResult.LoggedIn -> {
                sessionManager.saveSession(token, result.user)
                Result.Valid
            }
            CheckLoginResult.LoggedOut -> {
                sessionManager.clear()
                Result.NeedsLogin
            }
            CheckLoginResult.NetworkError,
            is CheckLoginResult.ServerError -> Result.OfflineValid
        }
    }
}
