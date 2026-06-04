package com.example.commov.data.remote

import com.example.commov.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class AdminApi(private val baseUrl: String = BuildConfig.API_BASE_URL) {
    fun users(token: String): AdminUsersResult {
        val connection = authedConnection("/users", token, "GET")

        return try {
            val responseBody = connection.readBody()
            when (connection.responseCode) {
                HttpURLConnection.HTTP_OK -> AdminUsersResult.Success(parseUsers(JSONArray(responseBody)))
                HttpURLConnection.HTTP_UNAUTHORIZED -> AdminUsersResult.Unauthorized
                HttpURLConnection.HTTP_FORBIDDEN -> AdminUsersResult.Forbidden
                else -> AdminUsersResult.ServerError(errorMessage(responseBody))
            }
        } catch (_: IOException) {
            AdminUsersResult.NetworkError
        } catch (_: Exception) {
            AdminUsersResult.ServerError(null)
        } finally {
            connection.disconnect()
        }
    }

    fun createUser(token: String, input: CreateUserInput): AdminMutationResult {
        val connection = authedConnection("/users", token, "POST").apply {
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }

        return try {
            val body = JSONObject()
                .put("name", input.name)
                .put("username", input.username)
                .put("email", input.email)
                .put("password", input.password)
                .put("photo", "")
                .put("role", input.role)
                .toString()

            connection.outputStream.use { output ->
                output.write(body.toByteArray(Charsets.UTF_8))
            }

            val responseBody = connection.readBody()
            when (connection.responseCode) {
                HttpURLConnection.HTTP_CREATED -> AdminMutationResult.Success
                HttpURLConnection.HTTP_UNAUTHORIZED -> AdminMutationResult.Unauthorized
                HttpURLConnection.HTTP_FORBIDDEN -> AdminMutationResult.Forbidden
                else -> AdminMutationResult.ServerError(errorMessage(responseBody))
            }
        } catch (_: IOException) {
            AdminMutationResult.NetworkError
        } catch (_: Exception) {
            AdminMutationResult.ServerError(null)
        } finally {
            connection.disconnect()
        }
    }

    fun deleteUser(token: String, userId: Int): AdminMutationResult {
        val connection = authedConnection("/users/$userId", token, "DELETE")

        return try {
            val responseBody = connection.readBody()
            when (connection.responseCode) {
                HttpURLConnection.HTTP_NO_CONTENT -> AdminMutationResult.Success
                HttpURLConnection.HTTP_UNAUTHORIZED -> AdminMutationResult.Unauthorized
                HttpURLConnection.HTTP_FORBIDDEN -> AdminMutationResult.Forbidden
                else -> AdminMutationResult.ServerError(errorMessage(responseBody))
            }
        } catch (_: IOException) {
            AdminMutationResult.NetworkError
        } catch (_: Exception) {
            AdminMutationResult.ServerError(null)
        } finally {
            connection.disconnect()
        }
    }

    private fun authedConnection(path: String, token: String, method: String): HttpURLConnection {
        return (URL("${baseUrl.trimEnd('/')}$path").openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
        }
    }

    private fun HttpURLConnection.readBody(): String {
        return if (responseCode in 200..299) {
            inputStream.bufferedReader().use { it.readText() }
        } else {
            errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
        }
    }

    private fun parseUsers(array: JSONArray): List<ApiUser> {
        return (0 until array.length()).map { index ->
            val json = array.getJSONObject(index)
            ApiUser(
                userId = json.getInt("userId"),
                name = json.getString("name"),
                email = json.getString("email"),
                role = json.optString("role")
            )
        }
    }

    private fun errorMessage(responseBody: String): String? {
        return runCatching { JSONObject(responseBody).optString("error").takeIf { it.isNotBlank() } }
            .getOrNull()
    }
}

data class CreateUserInput(
    val name: String,
    val username: String,
    val email: String,
    val password: String,
    val role: String
)

sealed interface AdminUsersResult {
    data class Success(val users: List<ApiUser>) : AdminUsersResult
    data object Unauthorized : AdminUsersResult
    data object Forbidden : AdminUsersResult
    data object NetworkError : AdminUsersResult
    data class ServerError(val message: String?) : AdminUsersResult
}

sealed interface AdminMutationResult {
    data object Success : AdminMutationResult
    data object Unauthorized : AdminMutationResult
    data object Forbidden : AdminMutationResult
    data object NetworkError : AdminMutationResult
    data class ServerError(val message: String?) : AdminMutationResult
}
