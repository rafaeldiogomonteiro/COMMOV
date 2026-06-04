package com.example.commov.data.remote

import com.example.commov.BuildConfig
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class PhotoApi(private val baseUrl: String = BuildConfig.API_BASE_URL) {
    fun uploadPhoto(token: String, fileName: String, bytes: ByteArray, mimeType: String = "image/jpeg"): PhotoUploadResult {
        val boundary = "----CommovBoundary${UUID.randomUUID()}"
        val connection = (URL("${baseUrl.trimEnd('/')}/photos/image").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 15_000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        }

        return try {
            connection.outputStream.use { output ->
                val lineEnd = "\r\n"
                val safeName = fileName.substringAfterLast('/').ifBlank { "photo.jpg" }
                output.write("--$boundary$lineEnd".toByteArray())
                output.write("Content-Disposition: form-data; name=\"photo\"; filename=\"$safeName\"$lineEnd".toByteArray())
                output.write("Content-Type: $mimeType$lineEnd$lineEnd".toByteArray())
                output.write(bytes)
                output.write("$lineEnd--$boundary--$lineEnd".toByteArray())
            }

            val responseBody = if (connection.responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }

            when (connection.responseCode) {
                HttpURLConnection.HTTP_CREATED -> {
                    val path = JSONObject(responseBody).optString("path")
                    PhotoUploadResult.Success(path)
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> PhotoUploadResult.Unauthorized
                else -> PhotoUploadResult.ServerError(errorMessage(responseBody))
            }
        } catch (_: IOException) {
            PhotoUploadResult.NetworkError
        } catch (_: Exception) {
            PhotoUploadResult.ServerError(null)
        } finally {
            connection.disconnect()
        }
    }

    fun photoUrl(path: String): String {
        val normalized = path.trim().removePrefix("/")
        return if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            normalized
        } else {
            "${baseUrl.trimEnd('/')}/$normalized"
        }
    }

    private fun errorMessage(responseBody: String): String? {
        return runCatching { JSONObject(responseBody).optString("error").takeIf { it.isNotBlank() } }.getOrNull()
    }
}

sealed interface PhotoUploadResult {
    data class Success(val path: String) : PhotoUploadResult
    data object Unauthorized : PhotoUploadResult
    data object NetworkError : PhotoUploadResult
    data class ServerError(val message: String?) : PhotoUploadResult
}
