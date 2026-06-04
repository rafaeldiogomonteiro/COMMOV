package com.example.commov.data.remote

import com.example.commov.BuildConfig
import com.example.commov.data.local.AuthenticatedUser
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class AuthApi(private val baseUrl: String = BuildConfig.API_BASE_URL) {
    fun login(email: String, password: String): LoginResult {
        val connection = (URL("${baseUrl.trimEnd('/')}/login").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 10_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }

        return try {
            val body = JSONObject()
                .put("email", email)
                .put("password", password)
                .toString()

            connection.outputStream.use { output ->
                output.write(body.toByteArray(Charsets.UTF_8))
            }

            val responseBody = if (connection.responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }

            when (connection.responseCode) {
                HttpURLConnection.HTTP_OK -> parseLogin(responseBody)
                HttpURLConnection.HTTP_UNAUTHORIZED -> LoginResult.InvalidCredentials
                else -> LoginResult.ServerError(errorMessage(responseBody))
            }
        } catch (_: IOException) {
            LoginResult.NetworkError
        } catch (_: Exception) {
            LoginResult.ServerError(null)
        } finally {
            connection.disconnect()
        }
    }

    fun checkLogin(token: String): CheckLoginResult {
        val connection = (URL("${baseUrl.trimEnd('/')}/check-login").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
        }

        return try {
            val responseBody = if (connection.responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }

            when (connection.responseCode) {
                HttpURLConnection.HTTP_OK -> parseCheckLogin(responseBody)
                HttpURLConnection.HTTP_UNAUTHORIZED -> CheckLoginResult.LoggedOut
                else -> CheckLoginResult.ServerError(errorMessage(responseBody))
            }
        } catch (_: IOException) {
            CheckLoginResult.NetworkError
        } catch (_: Exception) {
            CheckLoginResult.ServerError(null)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseLogin(responseBody: String): LoginResult {
        val json = JSONObject(responseBody)

        return LoginResult.Success(
            token = json.getString("token"),
            user = parseUser(json.getJSONObject("user"))
        )
    }

    private fun parseCheckLogin(responseBody: String): CheckLoginResult {
        val json = JSONObject(responseBody)
        if (!json.optBoolean("loggedIn", false)) {
            return CheckLoginResult.LoggedOut
        }

        return CheckLoginResult.LoggedIn(parseUser(json.getJSONObject("user")))
    }

    private fun parseUser(user: JSONObject): AuthenticatedUser {
        return AuthenticatedUser(
            userId = user.getInt("userId"),
            name = user.getString("name"),
            email = user.getString("email"),
            role = user.getString("role")
        )
    }

    private fun errorMessage(responseBody: String): String? {
        return runCatching { JSONObject(responseBody).optString("error").takeIf { it.isNotBlank() } }
            .getOrNull()
    }
}

sealed interface LoginResult {
    data class Success(val token: String, val user: AuthenticatedUser) : LoginResult
    data object InvalidCredentials : LoginResult
    data object NetworkError : LoginResult
    data class ServerError(val message: String?) : LoginResult
}

sealed interface CheckLoginResult {
    data class LoggedIn(val user: AuthenticatedUser) : CheckLoginResult
    data object LoggedOut : CheckLoginResult
    data object NetworkError : CheckLoginResult
    data class ServerError(val message: String?) : CheckLoginResult
}
