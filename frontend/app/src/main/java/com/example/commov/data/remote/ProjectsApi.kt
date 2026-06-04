package com.example.commov.data.remote

import com.example.commov.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class ProjectsApi(private val baseUrl: String = BuildConfig.API_BASE_URL) {
    fun projects(token: String): ProjectsResult {
        val projectsResult = getArray("/projects", token)
        if (projectsResult !is RemoteArrayResult.Success) {
            return projectsResult.toProjectsResult()
        }

        val projects = parseProjects(projectsResult.array).map { project ->
            val tasks = when (val tasksResult = getArray("/projects/${project.projectId}/tasks", token)) {
                is RemoteArrayResult.Success -> parseTasks(tasksResult.array)
                RemoteArrayResult.Unauthorized -> return ProjectsResult.Unauthorized
                RemoteArrayResult.NetworkError -> emptyList()
                is RemoteArrayResult.ServerError -> emptyList()
            }
            val members = when (val membersResult = getArray("/projects/${project.projectId}/users", token)) {
                is RemoteArrayResult.Success -> parseUsers(membersResult.array)
                RemoteArrayResult.Unauthorized -> return ProjectsResult.Unauthorized
                RemoteArrayResult.NetworkError -> emptyList()
                is RemoteArrayResult.ServerError -> emptyList()
            }

            project.copy(tasks = tasks, members = members)
        }

        return ProjectsResult.Success(projects)
    }

    fun users(token: String): UsersResult {
        return when (val usersResult = getArray("/users", token)) {
            is RemoteArrayResult.Success -> UsersResult.Success(parseUsers(usersResult.array))
            RemoteArrayResult.Unauthorized -> UsersResult.Unauthorized
            RemoteArrayResult.NetworkError -> UsersResult.NetworkError
            is RemoteArrayResult.ServerError -> UsersResult.ServerError(usersResult.message)
        }
    }

    fun projectUsers(token: String, projectId: Int): UsersResult {
        return when (val usersResult = getArray("/projects/$projectId/users", token)) {
            is RemoteArrayResult.Success -> UsersResult.Success(parseUsers(usersResult.array))
            RemoteArrayResult.Unauthorized -> UsersResult.Unauthorized
            RemoteArrayResult.NetworkError -> UsersResult.NetworkError
            is RemoteArrayResult.ServerError -> UsersResult.ServerError(usersResult.message)
        }
    }

    fun createProject(token: String, input: CreateProjectInput): CreateProjectResult {
        val connection = (URL("${baseUrl.trimEnd('/')}/projects").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 10_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
        }

        return try {
            val body = JSONObject()
                .put("name", input.name)
                .put("description", input.description)
                .put("managerId", input.managerId)
                .put("startDate", input.startDate)
                .put("estimatedEndDate", input.estimatedEndDate)
                .put("memberIds", JSONArray(input.memberIds))
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
                HttpURLConnection.HTTP_CREATED -> CreateProjectResult.Success
                HttpURLConnection.HTTP_UNAUTHORIZED -> CreateProjectResult.Unauthorized
                else -> CreateProjectResult.ServerError(errorMessage(responseBody))
            }
        } catch (_: IOException) {
            CreateProjectResult.NetworkError
        } catch (_: Exception) {
            CreateProjectResult.ServerError(null)
        } finally {
            connection.disconnect()
        }
    }

    fun createTask(token: String, projectId: Int, input: CreateTaskInput): CreateTaskResult {
        val connection = (URL("${baseUrl.trimEnd('/')}/projects/$projectId/tasks").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 10_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
        }

        return try {
            val body = JSONObject()
                .put("userId", input.userId)
                .put("title", input.title)
                .put("description", input.description)
                .put("estimatedEndDate", input.estimatedEndDate)
                .put("estimatedTime", input.estimatedTime)
                .put("location", input.location)
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
                HttpURLConnection.HTTP_CREATED -> CreateTaskResult.Success
                HttpURLConnection.HTTP_UNAUTHORIZED -> CreateTaskResult.Unauthorized
                else -> CreateTaskResult.ServerError(errorMessage(responseBody))
            }
        } catch (_: IOException) {
            CreateTaskResult.NetworkError
        } catch (_: Exception) {
            CreateTaskResult.ServerError(null)
        } finally {
            connection.disconnect()
        }
    }

    private fun getArray(path: String, token: String): RemoteArrayResult {
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
                HttpURLConnection.HTTP_OK -> RemoteArrayResult.Success(JSONArray(responseBody))
                HttpURLConnection.HTTP_UNAUTHORIZED -> RemoteArrayResult.Unauthorized
                else -> RemoteArrayResult.ServerError(errorMessage(responseBody))
            }
        } catch (_: IOException) {
            RemoteArrayResult.NetworkError
        } catch (_: Exception) {
            RemoteArrayResult.ServerError(null)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseProjects(array: JSONArray): List<ApiProject> {
        return (0 until array.length()).map { index ->
            val json = array.getJSONObject(index)
            ApiProject(
                projectId = json.getInt("projectId"),
                name = json.getString("name"),
                description = json.optString("description"),
                status = json.optString("status", "active"),
                tasks = emptyList(),
                members = emptyList()
            )
        }
    }

    private fun parseTasks(array: JSONArray): List<ApiTask> {
        return (0 until array.length()).map { index ->
            val json = array.getJSONObject(index)
            ApiTask(
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

    private fun JSONObject.optNullableString(name: String): String? {
        if (isNull(name)) {
            return null
        }

        return optString(name).takeIf { it.isNotBlank() }
    }

    private fun RemoteArrayResult.toProjectsResult(): ProjectsResult {
        return when (this) {
            is RemoteArrayResult.Success -> ProjectsResult.Success(emptyList())
            RemoteArrayResult.Unauthorized -> ProjectsResult.Unauthorized
            RemoteArrayResult.NetworkError -> ProjectsResult.NetworkError
            is RemoteArrayResult.ServerError -> ProjectsResult.ServerError(message)
        }
    }
}

data class ApiProject(
    val projectId: Int,
    val name: String,
    val description: String,
    val status: String,
    val tasks: List<ApiTask>,
    val members: List<ApiUser>
)

data class ApiUser(
    val userId: Int,
    val name: String,
    val email: String,
    val role: String
)

sealed interface ProjectsResult {
    data class Success(val projects: List<ApiProject>) : ProjectsResult
    data object Unauthorized : ProjectsResult
    data object NetworkError : ProjectsResult
    data class ServerError(val message: String?) : ProjectsResult
}

data class CreateProjectInput(
    val name: String,
    val description: String,
    val managerId: Int,
    val startDate: String,
    val estimatedEndDate: String,
    val memberIds: List<Int>
)

data class CreateTaskInput(
    val userId: Int,
    val title: String,
    val description: String,
    val estimatedEndDate: String,
    val estimatedTime: Double,
    val location: String
)

sealed interface UsersResult {
    data class Success(val users: List<ApiUser>) : UsersResult
    data object Unauthorized : UsersResult
    data object NetworkError : UsersResult
    data class ServerError(val message: String?) : UsersResult
}

sealed interface CreateProjectResult {
    data object Success : CreateProjectResult
    data object Unauthorized : CreateProjectResult
    data object NetworkError : CreateProjectResult
    data class ServerError(val message: String?) : CreateProjectResult
}

sealed interface CreateTaskResult {
    data object Success : CreateTaskResult
    data object Unauthorized : CreateTaskResult
    data object NetworkError : CreateTaskResult
    data class ServerError(val message: String?) : CreateTaskResult
}

private sealed interface RemoteArrayResult {
    data class Success(val array: JSONArray) : RemoteArrayResult
    data object Unauthorized : RemoteArrayResult
    data object NetworkError : RemoteArrayResult
    data class ServerError(val message: String?) : RemoteArrayResult
}
