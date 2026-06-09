package com.example.commov.data.remote

import com.example.commov.BuildConfig
import com.example.commov.data.local.AuthenticatedUser
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class UserApi(private val baseUrl: String = BuildConfig.API_BASE_URL) {
    fun updateProfile(token: String, photo: String): ProfileUpdateResult {
        val connection = (URL("${baseUrl.trimEnd('/')}/users/me").openConnection() as HttpURLConnection).apply {
            requestMethod = "PATCH"
            connectTimeout = 10_000
            readTimeout = 10_000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
        }

        return try {
            val body = JSONObject().put("photo", photo).toString()
            connection.outputStream.use { output ->
                output.write(body.toByteArray(Charsets.UTF_8))
            }

            val responseBody = if (connection.responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }

            when (connection.responseCode) {
                HttpURLConnection.HTTP_OK -> ProfileUpdateResult.Success(parseUser(JSONObject(responseBody)))
                HttpURLConnection.HTTP_UNAUTHORIZED -> ProfileUpdateResult.Unauthorized
                else -> ProfileUpdateResult.ServerError(errorMessage(responseBody))
            }
        } catch (_: IOException) {
            ProfileUpdateResult.NetworkError
        } catch (_: Exception) {
            ProfileUpdateResult.ServerError(null)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseUser(json: JSONObject): AuthenticatedUser {
        return AuthenticatedUser(
            userId = json.getInt("userId"),
            name = json.getString("name"),
            email = json.getString("email"),
            role = json.getString("role"),
            photo = json.optString("photo")
        )
    }

    private fun errorMessage(responseBody: String): String? {
        return runCatching { JSONObject(responseBody).optString("error").takeIf { it.isNotBlank() } }
            .getOrNull()
    }
}

sealed interface ProfileUpdateResult {
    data class Success(val user: AuthenticatedUser) : ProfileUpdateResult
    data object Unauthorized : ProfileUpdateResult
    data object NetworkError : ProfileUpdateResult
    data class ServerError(val message: String?) : ProfileUpdateResult
}
