package com.example.commov.data.remote

import com.example.commov.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class StatisticsApi(private val baseUrl: String = BuildConfig.API_BASE_URL) {
    fun users(token: String): StatisticsUsersResult {
        val connection = authedConnection("/statistics/users", token, "GET")

        return try {
            val responseBody = connection.readBody()
            when (connection.responseCode) {
                HttpURLConnection.HTTP_OK -> StatisticsUsersResult.Success(parseUsers(JSONArray(responseBody)))
                HttpURLConnection.HTTP_UNAUTHORIZED -> StatisticsUsersResult.Unauthorized
                HttpURLConnection.HTTP_FORBIDDEN -> StatisticsUsersResult.Forbidden
                else -> StatisticsUsersResult.ServerError(errorMessage(responseBody))
            }
        } catch (_: IOException) {
            StatisticsUsersResult.NetworkError
        } catch (_: Exception) {
            StatisticsUsersResult.ServerError(null)
        } finally {
            connection.disconnect()
        }
    }

    fun exportUserReport(token: String, userId: Int): StatisticsExportResult {
        return exportPdf(token, "/statistics/users/$userId/export")
    }

    fun exportProjectReport(token: String, projectId: Int): StatisticsExportResult {
        return exportPdf(token, "/statistics/projects/$projectId/export")
    }

    fun exportProjectTasksReport(token: String, projectId: Int): StatisticsExportResult {
        return exportPdf(token, "/statistics/projects/$projectId/tasks/export")
    }

    private fun exportPdf(token: String, path: String): StatisticsExportResult {
        val connection = authedConnection(path, token, "GET").apply {
            setRequestProperty("Accept", "application/pdf")
        }

        return try {
            when (connection.responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val bytes = connection.inputStream.use { it.readBytes() }
                    val filename = connection.contentDispositionFilename()
                        ?: "commov-statistics-report.pdf"
                    StatisticsExportResult.Success(bytes, filename)
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> StatisticsExportResult.Unauthorized
                HttpURLConnection.HTTP_FORBIDDEN -> StatisticsExportResult.Forbidden
                else -> {
                    val responseBody = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                    StatisticsExportResult.ServerError(errorMessage(responseBody))
                }
            }
        } catch (_: IOException) {
            StatisticsExportResult.NetworkError
        } catch (_: Exception) {
            StatisticsExportResult.ServerError(null)
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

sealed interface StatisticsUsersResult {
    data class Success(val users: List<ApiUser>) : StatisticsUsersResult
    data object Unauthorized : StatisticsUsersResult
    data object Forbidden : StatisticsUsersResult
    data object NetworkError : StatisticsUsersResult
    data class ServerError(val message: String?) : StatisticsUsersResult
}

sealed interface StatisticsExportResult {
    data class Success(val bytes: ByteArray, val filename: String) : StatisticsExportResult
    data object Unauthorized : StatisticsExportResult
    data object Forbidden : StatisticsExportResult
    data object NetworkError : StatisticsExportResult
    data class ServerError(val message: String?) : StatisticsExportResult
}
