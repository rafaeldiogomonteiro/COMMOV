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
                .put("active", input.active)
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

    fun updateUser(token: String, userId: Int, input: UpdateUserInput): AdminMutationResult {
        val connection = authedConnection("/users/$userId", token, "PUT").apply {
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }

        return try {
            val body = JSONObject()
            input.name?.let { body.put("name", it) }
            input.username?.let { body.put("username", it) }
            input.email?.let { body.put("email", it) }
            input.password?.let { body.put("password", it) }
            input.photo?.let { body.put("photo", it) }
            input.role?.let { body.put("role", it) }
            input.active?.let { body.put("active", it) }

            connection.outputStream.use { output ->
                output.write(body.toString().toByteArray(Charsets.UTF_8))
            }

            val responseBody = connection.readBody()
            when (connection.responseCode) {
                HttpURLConnection.HTTP_OK -> AdminMutationResult.Success
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

    fun changePassword(token: String, userId: Int, password: String): AdminMutationResult {
        val connection = authedConnection("/users/$userId/password", token, "PATCH").apply {
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }

        return try {
            val body = JSONObject().put("password", password).toString()
            connection.outputStream.use { output ->
                output.write(body.toByteArray(Charsets.UTF_8))
            }

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

    fun exportUserReport(token: String, userId: Int): AdminExportResult {
        val connection = authedConnection("/users/$userId/export", token, "GET").apply {
            setRequestProperty("Accept", "application/pdf")
        }

        return try {
            when (connection.responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val bytes = connection.inputStream.use { it.readBytes() }
                    val filename = connection.contentDispositionFilename()
                        ?: "commov-user-$userId-report.pdf"
                    AdminExportResult.Success(bytes, filename)
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> AdminExportResult.Unauthorized
                HttpURLConnection.HTTP_FORBIDDEN -> AdminExportResult.Forbidden
                else -> {
                    val responseBody = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                    AdminExportResult.ServerError(errorMessage(responseBody))
                }
            }
        } catch (_: IOException) {
            AdminExportResult.NetworkError
        } catch (_: Exception) {
            AdminExportResult.ServerError(null)
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
                username = json.optString("username"),
                email = json.getString("email"),
                role = json.optString("role"),
                active = json.optBoolean("active", true),
                photo = json.optString("photo")
            )
        }
    }

    private fun errorMessage(responseBody: String): String? {
        return runCatching { JSONObject(responseBody).optString("error").takeIf { it.isNotBlank() } }
            .getOrNull()
    }

    private fun HttpURLConnection.contentDispositionFilename(): String? {
        val header = getHeaderField("Content-Disposition") ?: return null
        val filenameMarker = "filename="
        val startIndex = header.indexOf(filenameMarker, ignoreCase = true)
        if (startIndex < 0) return null
        var filename = header.substring(startIndex + filenameMarker.length).trim()
        if (filename.startsWith("\"") && filename.endsWith("\"")) {
            filename = filename.substring(1, filename.length - 1)
        }
        return filename.takeIf { it.isNotBlank() }
    }
}

data class CreateUserInput(
    val name: String,
    val username: String,
    val email: String,
    val password: String,
    val role: String,
    val active: Boolean = true
)

data class UpdateUserInput(
    val name: String? = null,
    val username: String? = null,
    val email: String? = null,
    val password: String? = null,
    val photo: String? = null,
    val role: String? = null,
    val active: Boolean? = null
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

sealed interface AdminExportResult {
    data class Success(val bytes: ByteArray, val filename: String) : AdminExportResult
    data object Unauthorized : AdminExportResult
    data object Forbidden : AdminExportResult
    data object NetworkError : AdminExportResult
    data class ServerError(val message: String?) : AdminExportResult
}
