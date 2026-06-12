package com.example.commov.data.remote

import com.example.commov.BuildConfig
import com.example.commov.model.Status
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class DashboardApi(private val baseUrl: String = BuildConfig.API_BASE_URL) {
    fun dashboard(token: String): DashboardResult {
        val tasksResult = getArray("/tasks", token)
        if (tasksResult !is ArrayResult.Success) {
            return tasksResult.toDashboardResult()
        }

        val projects = when (val projectsResult = getArray("/projects", token)) {
            is ArrayResult.Success -> parseProjects(projectsResult.array)
            ArrayResult.Unauthorized -> return DashboardResult.Unauthorized
            ArrayResult.NetworkError -> emptyList()
            is ArrayResult.ServerError -> emptyList()
        }

        return DashboardResult.Success(
            tasks = parseTasks(tasksResult.array),
            projects = projects
        )
    }

    private fun getArray(path: String, token: String): ArrayResult {
        val connection = (URL("${baseUrl.trimEnd('/')}$path").openConnection() as HttpURLConnection).apply {
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
                HttpURLConnection.HTTP_OK -> ArrayResult.Success(JSONArray(responseBody))
                HttpURLConnection.HTTP_UNAUTHORIZED -> ArrayResult.Unauthorized
                else -> ArrayResult.ServerError(errorMessage(responseBody))
            }
        } catch (_: IOException) {
            ArrayResult.NetworkError
        } catch (_: Exception) {
            ArrayResult.ServerError(null)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseTasks(array: JSONArray): List<ApiTask> {
        return (0 until array.length()).map { index ->
            val json = array.getJSONObject(index)
            ApiTask(
                taskId = json.getInt("taskId"),
                projectId = json.getInt("projectId"),
                userIds = json.parseUserIds(),
                title = json.getString("title"),
                description = json.optString("description"),
                status = Status.normalizeTaskStatus(json.optString("status")),
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
    }

    private fun parseProjects(array: JSONArray): List<ApiProjectSummary> {
        return (0 until array.length()).map { index ->
            val json = array.getJSONObject(index)
            ApiProjectSummary(
                projectId = json.getInt("projectId"),
                name = json.getString("name"),
                description = json.optString("description"),
                status = json.optString("status", "active")
            )
        }
    }

    private fun errorMessage(responseBody: String): String? {
        return runCatching { JSONObject(responseBody).optString("error").takeIf { it.isNotBlank() } }
            .getOrNull()
    }

    private fun JSONObject.optNullableString(name: String): String? {
        if (isNull(name)) {
            return null
        }

        return optString(name).takeIf { it.isNotBlank() }
    }

    private fun JSONObject.parseUserIds(): List<Int> {
        val array = optJSONArray("userIds") ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) {
                add(array.getInt(index))
            }
        }
    }

    private fun ArrayResult.toDashboardResult(): DashboardResult {
        return when (this) {
            is ArrayResult.Success -> DashboardResult.Success(emptyList(), emptyList())
            ArrayResult.Unauthorized -> DashboardResult.Unauthorized
            ArrayResult.NetworkError -> DashboardResult.NetworkError
            is ArrayResult.ServerError -> DashboardResult.ServerError(message)
        }
    }
}

data class ApiTask(
    val taskId: Int,
    val projectId: Int,
    val userIds: List<Int> = emptyList(),
    val title: String,
    val description: String,
    val status: String,
    val estimatedEndDate: String?,
    val actualEndDate: String?,
    val estimatedTime: Double,
    val timeSpent: Double,
    val completionRate: Double,
    val workDate: String?,
    val location: String,
    val observation: String,
    val photo: String
)

data class ApiProjectSummary(
    val projectId: Int,
    val name: String,
    val description: String,
    val status: String
)

sealed interface DashboardResult {
    data class Success(
        val tasks: List<ApiTask>,
        val projects: List<ApiProjectSummary>
    ) : DashboardResult

    data object Unauthorized : DashboardResult
    data object NetworkError : DashboardResult
    data class ServerError(val message: String?) : DashboardResult
}

private sealed interface ArrayResult {
    data class Success(val array: JSONArray) : ArrayResult
    data object Unauthorized : ArrayResult
    data object NetworkError : ArrayResult
    data class ServerError(val message: String?) : ArrayResult
}
