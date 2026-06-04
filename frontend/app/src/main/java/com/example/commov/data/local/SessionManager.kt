package com.example.commov.data.local

import android.content.Context

class SessionManager(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun saveSession(token: String, user: AuthenticatedUser) {
        preferences.edit()
            .putString(KEY_TOKEN, token)
            .putInt(KEY_USER_ID, user.userId)
            .putString(KEY_USER_NAME, user.name)
            .putString(KEY_USER_EMAIL, user.email)
            .putString(KEY_USER_ROLE, user.role)
            .apply()
    }

    fun token(): String? = preferences.getString(KEY_TOKEN, null)

    fun currentUser(): AuthenticatedUser? {
        val token = token()
        val name = preferences.getString(KEY_USER_NAME, null)
        val email = preferences.getString(KEY_USER_EMAIL, null)
        val role = preferences.getString(KEY_USER_ROLE, null)

        if (token.isNullOrBlank() || name.isNullOrBlank() || email.isNullOrBlank() || role.isNullOrBlank()) {
            return null
        }

        return AuthenticatedUser(
            userId = preferences.getInt(KEY_USER_ID, 0),
            name = name,
            email = email,
            role = role
        )
    }

    fun canManageProjects(): Boolean {
        val role = preferences.getString(KEY_USER_ROLE, null)
        return role == "admin" || role == "project_manager"
    }

    fun isAdmin(): Boolean {
        return preferences.getString(KEY_USER_ROLE, null) == "admin"
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "commov_session"
        private const val KEY_TOKEN = "token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
    }
}

data class AuthenticatedUser(
    val userId: Int,
    val name: String,
    val email: String,
    val role: String
)
