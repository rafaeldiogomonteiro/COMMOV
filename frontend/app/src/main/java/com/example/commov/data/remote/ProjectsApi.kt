package com.example.commov.data.remote

import com.example.commov.BuildConfig
import com.example.commov.model.Status
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class ProjectsApi(private val baseUrl: String = BuildConfig.API_BASE_URL) {
    fun listProjects(token: String): ProjectsResult {
        return when (val projectsResult = getArray("/projects", token)) {
            is RemoteArrayResult.Success -> ProjectsResult.Success(parseProjects(projectsResult.array))
            RemoteArrayResult.Unauthorized -> ProjectsResult.Unauthorized
            RemoteArrayResult.NetworkError -> ProjectsResult.NetworkError
            is RemoteArrayResult.ServerError -> ProjectsResult.ServerError(projectsResult.message)
        }
    }

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

    fun updateProject(token: String, projectId: Int, input: UpdateProjectInput): ProjectMutationResult {
        return sendJson(
            token = token,
            path = "/projects/$projectId",
            method = "PUT",
            body = JSONObject().apply {
                input.name?.let { put("name", it) }
                input.description?.let { put("description", it) }
                input.status?.let { put("status", it) }
                input.managerId?.let { put("managerId", it) }
                input.startDate?.let { put("startDate", it) }
                input.estimatedEndDate?.let { put("estimatedEndDate", it) }
                input.actualEndDate?.let { put("actualEndDate", it) }
            }
        )
    }

    fun deleteProject(token: String, projectId: Int): ProjectMutationResult {
        val connection = authedConnection("/projects/$projectId", token, "DELETE")
        return try {
            val responseBody = connection.readBody()
            when (connection.responseCode) {
                HttpURLConnection.HTTP_NO_CONTENT -> ProjectMutationResult.Success
                HttpURLConnection.HTTP_UNAUTHORIZED -> ProjectMutationResult.Unauthorized
                else -> ProjectMutationResult.ServerError(errorMessage(responseBody))
            }
        } catch (_: IOException) {
            ProjectMutationResult.NetworkError
        } catch (_: Exception) {
            ProjectMutationResult.ServerError(null)
        } finally {
            connection.disconnect()
        }
    }

    fun getProject(token: String, projectId: Int): ProjectDetailResult {
        val connection = authedConnection("/projects/$projectId", token, "GET")
        return try {
            val responseBody = connection.readBody()
            when (connection.responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val json = JSONObject(responseBody)
                    ProjectDetailResult.Success(
                        ApiProject(
                            projectId = json.getInt("projectId"),
                            name = json.getString("name"),
                            description = json.optString("description"),
                            status = json.optString("status", "active"),
                            managerId = json.optInt("managerId"),
                            createdBy = json.optInt("createdBy"),
                            startDate = json.optNullableString("startDate"),
                            estimatedEndDate = json.optNullableString("estimatedEndDate"),
                            actualEndDate = json.optNullableString("actualEndDate"),
                            tasks = emptyList(),
                            members = emptyList()
                        )
                    )
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> ProjectDetailResult.Unauthorized
                else -> ProjectDetailResult.ServerError(errorMessage(responseBody))
            }
        } catch (_: IOException) {
            ProjectDetailResult.NetworkError
        } catch (_: Exception) {
            ProjectDetailResult.ServerError(null)
        } finally {
            connection.disconnect()
        }
    }

    fun addMember(token: String, projectId: Int, userId: Int): ProjectMutationResult {
        val connection = authedConnection("/projects/$projectId/users", token, "POST").apply {
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }
        return try {
            connection.outputStream.use {
                it.write(JSONObject().put("userId", userId).toString().toByteArray(Charsets.UTF_8))
            }
            val responseBody = connection.readBody()
            when (connection.responseCode) {
                HttpURLConnection.HTTP_NO_CONTENT -> ProjectMutationResult.Success
                HttpURLConnection.HTTP_UNAUTHORIZED -> ProjectMutationResult.Unauthorized
                else -> ProjectMutationResult.ServerError(errorMessage(responseBody))
            }
        } catch (_: IOException) {
            ProjectMutationResult.NetworkError
        } catch (_: Exception) {
            ProjectMutationResult.ServerError(null)
        } finally {
            connection.disconnect()
        }
    }

    fun removeMember(token: String, projectId: Int, userId: Int): ProjectMutationResult {
        val connection = authedConnection("/projects/$projectId/users/$userId", token, "DELETE")
        return try {
            val responseBody = connection.readBody()
            when (connection.responseCode) {
                HttpURLConnection.HTTP_NO_CONTENT -> ProjectMutationResult.Success
                HttpURLConnection.HTTP_UNAUTHORIZED -> ProjectMutationResult.Unauthorized
                else -> ProjectMutationResult.ServerError(errorMessage(responseBody))
            }
        } catch (_: IOException) {
            ProjectMutationResult.NetworkError
        } catch (_: Exception) {
            ProjectMutationResult.ServerError(null)
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
                .put("userIds", JSONArray(input.userIds))
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

    private fun sendJson(token: String, path: String, method: String, body: JSONObject): ProjectMutationResult {
        val connection = authedConnection(path, token, method).apply {
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }
        return try {
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val responseBody = connection.readBody()
            when (connection.responseCode) {
                HttpURLConnection.HTTP_OK -> ProjectMutationResult.Success
                HttpURLConnection.HTTP_UNAUTHORIZED -> ProjectMutationResult.Unauthorized
                else -> ProjectMutationResult.ServerError(errorMessage(responseBody))
            }
        } catch (_: IOException) {
            ProjectMutationResult.NetworkError
        } catch (_: Exception) {
            ProjectMutationResult.ServerError(null)
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

    private fun getArray(path: String, token: String): RemoteArrayResult {
        val connection = authedConnection(path, token, "GET")
        return try {
            val responseBody = connection.readBody()
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
                managerId = json.optInt("managerId"),
                createdBy = json.optInt("createdBy"),
                startDate = json.optNullableString("startDate"),
                estimatedEndDate = json.optNullableString("estimatedEndDate"),
                actualEndDate = json.optNullableString("actualEndDate"),
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
    val managerId: Int = 0,
    val createdBy: Int = 0,
    val startDate: String? = null,
    val estimatedEndDate: String? = null,
    val actualEndDate: String? = null,
    val tasks: List<ApiTask>,
    val members: List<ApiUser>
)

data class ApiUser(
    val userId: Int,
    val name: String,
    val username: String = "",
    val email: String,
    val role: String,
    val active: Boolean = true,
    val photo: String = ""
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

data class UpdateProjectInput(
    val name: String? = null,
    val description: String? = null,
    val status: String? = null,
    val managerId: Int? = null,
    val startDate: String? = null,
    val estimatedEndDate: String? = null,
    val actualEndDate: String? = null
)

sealed interface ProjectMutationResult {
    data object Success : ProjectMutationResult
    data object Unauthorized : ProjectMutationResult
    data object NetworkError : ProjectMutationResult
    data class ServerError(val message: String?) : ProjectMutationResult
}

data class CreateTaskInput(
    val userIds: List<Int>,
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

sealed interface ProjectDetailResult {
    data class Success(val project: ApiProject) : ProjectDetailResult
    data object Unauthorized : ProjectDetailResult
    data object NetworkError : ProjectDetailResult
    data class ServerError(val message: String?) : ProjectDetailResult
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
