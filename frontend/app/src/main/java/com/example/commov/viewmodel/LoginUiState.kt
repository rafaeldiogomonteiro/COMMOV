package com.example.commov.viewmodel

import androidx.annotation.StringRes

data class LoginUiState(
    val email: String,
    val password: String,
    val passwordVisible: Boolean,
    @StringRes val emailErrorResId: Int,
    @StringRes val passwordErrorResId: Int,
    val loginAccepted: Boolean
)
