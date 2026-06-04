package com.example.commov.data.remote

import com.example.commov.BuildConfig
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class TaskApi(private val baseUrl: String = BuildConfig.API_BASE_URL) {
    fun getTask(token: String, taskId: Int): TaskResult {
        val connection = authedConnection("/tasks/$taskId", token, "GET")
        return try {
            val body = connection.readBody()
            when (connection.responseCode) {
                HttpURLConnection.HTTP_OK -> TaskResult.Success(parseTask(JSONObject(body)))
                HttpURLConnection.HTTP_UNAUTHORIZED -> TaskResult.Unauthorized
                else -> TaskResult.ServerError(errorMessage(body))
            }
        } catch (_: IOException) {
            TaskResult.NetworkError
        } catch (_: Exception) {
            TaskResult.ServerError(null)
        } finally {
            connection.disconnect()
        }
    }

    fun addTimeSpent(token: String, taskId: Int, timeSpent: Double, workDate: String, observation: String): TaskMutationResult {
        val body = JSONObject()
            .put("timeSpent", timeSpent)
            .put("workDate", workDate)
            .put("observation", observation)
        return sendJson(token, "/tasks/$taskId/time-spent", "POST", body)
    }

    fun complete(token: String, taskId: Int, workDate: String, observation: String): TaskMutationResult {
        val body = JSONObject()
            .put("workDate", workDate)
            .put("observation", observation)
        return sendJson(token, "/tasks/$taskId/complete", "PATCH", body)
    }

    fun delete(token: String, taskId: Int): TaskMutationResult {
        val connection = authedConnection("/tasks/$taskId", token, "DELETE")
        return try {
            val body = connection.readBody()
            when (connection.responseCode) {
                HttpURLConnection.HTTP_NO_CONTENT -> TaskMutationResult.Success
                HttpURLConnection.HTTP_UNAUTHORIZED -> TaskMutationResult.Unauthorized
                else -> TaskMutationResult.ServerError(errorMessage(body))
            }
        } catch (_: IOException) {
            TaskMutationResult.NetworkError
        } catch (_: Exception) {
            TaskMutationResult.ServerError(null)
        } finally {
            connection.disconnect()
        }
    }

    private fun sendJson(token: String, path: String, method: String, body: JSONObject): TaskMutationResult {
        val connection = authedConnection(path, token, method).apply {
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }
        return try {
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val responseBody = connection.readBody()
            when (connection.responseCode) {
                HttpURLConnection.HTTP_OK -> TaskMutationResult.Success
                HttpURLConnection.HTTP_UNAUTHORIZED -> TaskMutationResult.Unauthorized
                else -> TaskMutationResult.ServerError(errorMessage(responseBody))
            }
        } catch (_: IOException) {
            TaskMutationResult.NetworkError
        } catch (_: Exception) {
            TaskMutationResult.ServerError(null)
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

    private fun parseTask(json: JSONObject): ApiTask {
        return ApiTask(
            taskId = json.getInt("taskId"),
            projectId = json.getInt("projectId"),
            userId = json.getInt("userId"),
            title = json.getString("title"),
            description = json.optString("description"),
            status = json.optString("status", "pending"),
            estimatedEndDate = json.optNullableString("estimatedEndDate"),
            actualEndDate = json.optNullableString("actualEndDate"),
            estimatedTime = json.optDouble("estimatedTime", 0.0),
            timeSpent = json.optDouble("timeSpent", 0.0),
            completionRate = json.optDouble("completionRate", 0.0),
            workDate = json.optNullableString("workDate"),
            location = json.optString("location"),
            observation = json.optString("observation"),
            photo = json.optString("photo")
        )
    }

    private fun JSONObject.optNullableString(name: String): String? {
        if (isNull(name)) return null
        return optString(name).takeIf { it.isNotBlank() }
    }

    private fun errorMessage(responseBody: String): String? {
        return runCatching { JSONObject(responseBody).optString("error").takeIf { it.isNotBlank() } }.getOrNull()
    }
}

sealed interface TaskResult {
    data class Success(val task: ApiTask) : TaskResult
    data object Unauthorized : TaskResult
    data object NetworkError : TaskResult
    data class ServerError(val message: String?) : TaskResult
}

sealed interface TaskMutationResult {
    data object Success : TaskMutationResult
    data object Unauthorized : TaskMutationResult
    data object NetworkError : TaskMutationResult
    data class ServerError(val message: String?) : TaskMutationResult
}
