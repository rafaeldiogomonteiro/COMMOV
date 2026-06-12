package com.example.commov.viewmodel

import androidx.annotation.StringRes

data class LoginUiState(
    val email: String,
    val password: String,
    val passwordVisible: Boolean,
    @StringRes val emailErrorResId: Int,
    @StringRes val passwordErrorResId: Int,
    @StringRes val generalErrorResId: Int,
    val isLoading: Boolean,
    val isCheckingSession: Boolean = false,
    val loginAccepted: Boolean
)
