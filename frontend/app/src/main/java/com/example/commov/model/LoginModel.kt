package com.example.commov.model

data class LoginModel(
    val email: String,
    val password: String,
    val passwordVisible: Boolean
) {
    fun withEmail(email: String) = copy(email = email)
    fun withPassword(password: String) = copy(password = password)
    fun withPasswordVisible(passwordVisible: Boolean) = copy(passwordVisible = passwordVisible)
}
