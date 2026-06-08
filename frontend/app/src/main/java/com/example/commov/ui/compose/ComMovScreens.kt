package com.example.commov.ui.compose

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.commov.MainActivity
import com.example.commov.R
import com.example.commov.data.local.SessionManager
import com.example.commov.data.local.LocaleHelper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.commov.data.remote.AuthApi
import com.example.commov.data.remote.AdminApi
import com.example.commov.data.remote.AdminMutationResult
import com.example.commov.data.remote.AdminUsersResult
import com.example.commov.data.remote.ApiTask
import com.example.commov.data.remote.ApiUser
import com.example.commov.data.remote.CreateUserInput
import com.example.commov.data.remote.CreateProjectInput
import com.example.commov.data.remote.CreateProjectResult
import com.example.commov.data.remote.CreateTaskInput as RemoteCreateTaskInput
import com.example.commov.data.remote.CreateTaskResult
import com.example.commov.data.remote.PhotoApi
import com.example.commov.data.remote.PhotoUploadResult
import com.example.commov.data.remote.ProjectMutationResult
import com.example.commov.data.remote.ProjectsApi
import com.example.commov.data.remote.TaskApi
import com.example.commov.data.remote.UpdateProjectInput
import com.example.commov.data.remote.UpdateUserInput
import com.example.commov.data.remote.TaskMutationResult
import com.example.commov.data.remote.TaskResult
import com.example.commov.data.remote.UsersResult
import com.example.commov.model.DashboardTask
import com.example.commov.model.Project
import com.example.commov.model.ProjectMember
import com.example.commov.model.ProjectTask
import com.example.commov.ui.admin.AdminActivity
import com.example.commov.ui.dashboard.DashboardActivity
import com.example.commov.ui.projects.CreateProjectActivity
import com.example.commov.ui.projects.CreateTaskActivity
import com.example.commov.ui.projects.ProjectsActivity
import com.example.commov.ui.projects.TaskDetailActivity
import com.example.commov.ui.settings.SettingsActivity
import com.example.commov.viewmodel.DashboardUiState
import com.example.commov.viewmodel.DashboardViewModel
import com.example.commov.viewmodel.LoginUiState
import com.example.commov.viewmodel.LoginViewModel
import com.example.commov.viewmodel.ProjectsUiState
import com.example.commov.viewmodel.ProjectsViewModel
import com.example.commov.viewmodel.SettingsViewModel
import java.util.Calendar
import java.util.Locale

enum class Destination {
    HOME,
    PROJECTS,
    ADMIN,
    SETTINGS
}

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val activity = context.findActivity()
    val viewModel = remember { LoginViewModel(context.applicationContext) }
    var state by remember { mutableStateOf(LoginUiState("", "", false, 0, 0, 0, false, false)) }

    DisposableEffect(viewModel) {
        viewModel.observe { state = it }
        onDispose { }
    }

    LaunchedEffect(state.loginAccepted) {
        if (state.loginAccepted) {
            activity.startActivity(Intent(activity, DashboardActivity::class.java))
            activity.finish()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.login_background))
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .cardBackground(R.color.login_card, R.color.login_card_stroke, 8.dp)
                .padding(start = 24.dp, top = 28.dp, end = 24.dp, bottom = 28.dp)
        ) {
            LanguageSelector(
                modifier = Modifier.align(Alignment.End),
                onLanguageSelected = { language ->
                    if (LocaleHelper.getSavedLanguage(context) != language) {
                        LocaleHelper.setLanguage(context, language)
                        restartRoot(context, MainActivity::class.java)
                    }
                }
            )
            Text(
                text = stringResource(R.string.app_name),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                color = colorResource(R.color.login_text_primary),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.login_title),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                color = colorResource(R.color.login_text_primary),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(R.string.login_subtitle),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                color = colorResource(R.color.login_text_secondary),
                fontSize = 14.sp,
                lineHeight = 18.sp
            )
            Text(
                text = stringResource(R.string.login_email_label),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 26.dp),
                color = colorResource(R.color.login_text_secondary),
                fontSize = 16.sp
            )
            LoginInput(
                value = state.email,
                onValueChange = viewModel::onEmailChanged,
                iconResId = R.drawable.ic_mail,
                contentDescription = stringResource(R.string.content_email_icon),
                keyboardType = KeyboardType.Email,
                modifier = Modifier.padding(top = 6.dp)
            )
            ErrorText(state.emailErrorResId)
            Text(
                text = stringResource(R.string.login_password_label),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                color = colorResource(R.color.login_text_secondary),
                fontSize = 14.sp
            )
            LoginInput(
                value = state.password,
                onValueChange = viewModel::onPasswordChanged,
                iconResId = R.drawable.ic_lock,
                contentDescription = stringResource(R.string.content_password_icon),
                keyboardType = KeyboardType.Password,
                visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButtonLike(
                        iconResId = R.drawable.ic_eye,
                        contentDescription = stringResource(R.string.content_toggle_password),
                        tint = Color.Unspecified,
                        modifier = Modifier.size(38.dp),
                        onClick = viewModel::onTogglePasswordVisibility
                    )
                },
                modifier = Modifier.padding(top = 6.dp)
            )
            ErrorText(state.passwordErrorResId)
            ErrorText(state.generalErrorResId)
            FilledActionButton(
                text = stringResource(if (state.isLoading) R.string.login_loading else R.string.login_button),
                iconResId = R.drawable.ic_arrow_right,
                colorResId = R.color.login_button,
                radius = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .height(48.dp),
                onClick = {
                    if (!state.isLoading) {
                        viewModel.onLoginClicked()
                    }
                }
            )
        }
    }
}

@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val activity = context.findActivity()
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val viewModel = remember { DashboardViewModel(context.applicationContext) }
    var state by remember {
        mutableStateOf(
            DashboardUiState(
                userName = "",
                pendingTasks = 0,
                completedTasks = 0,
                pendingProgress = 0,
                completedProgress = 0,
                tasks = emptyList(),
                requiresLogin = false
            )
        )
    }

    DisposableEffect(viewModel) {
        viewModel.observe { state = it }
        onDispose { }
    }

    LaunchedEffect(state.requiresLogin) {
        if (state.requiresLogin) {
            activity.startActivity(Intent(activity, MainActivity::class.java))
            activity.finish()
        }
    }

    AppScaffold(selectedDestination = Destination.HOME) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .screenContentPadding()
        ) {
            Text(
                text = stringResource(
                    R.string.dashboard_greeting,
                    state.userName.ifBlank { sessionManager.currentUser()?.name?.trim().orEmpty() }.ifBlank { "…" }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                color = colorResource(R.color.dashboard_text_primary),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashboardSummaryCard(
                    titleResId = R.string.dashboard_pending_tasks,
                    count = state.pendingTasks,
                    progress = state.pendingProgress,
                    iconResId = R.drawable.ic_calendar_clock,
                    progressColorResId = R.color.task_red,
                    modifier = Modifier.weight(1f)
                )
                DashboardSummaryCard(
                    titleResId = R.string.dashboard_completed_tasks,
                    count = state.completedTasks,
                    progress = state.completedProgress,
                    iconResId = R.drawable.ic_check_circle,
                    progressColorResId = R.color.project_green,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_my_tasks),
                    modifier = Modifier.weight(1f),
                    color = colorResource(R.color.dashboard_text_primary),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.dashboard_see_all),
                    modifier = Modifier
                        .clickable {
                            context.startActivity(Intent(context, ProjectsActivity::class.java))
                        }
                        .padding(vertical = 8.dp),
                    color = colorResource(R.color.bottom_nav_selected),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.tasks.isEmpty()) {
                    Text(
                        text = stringResource(R.string.dashboard_empty_tasks),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        color = colorResource(R.color.dashboard_text_secondary),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
                state.tasks.forEach { task ->
                    DashboardTaskCard(
                        task = task,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(66.dp)
                            .padding(bottom = 8.dp),
                        onClick = {
                            if (task.taskId > 0) {
                                val intent = Intent(context, TaskDetailActivity::class.java)
                                intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, task.taskId)
                                context.startActivity(intent)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectsScreen() {
    val context = LocalContext.current
    val activity = context.findActivity()
    val viewModel = remember { ProjectsViewModel(context.applicationContext) }
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val projectsApi = remember { ProjectsApi() }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val canManage = remember { sessionManager.canManageProjects() }
    var state by remember {
        mutableStateOf(
            ProjectsUiState(
                projects = emptyList(),
                canCreateTasks = false,
                requiresLogin = false
            )
        )
    }
    var selectedProjectId by remember { mutableStateOf<Int?>(null) }
    var showEditProject by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editStatus by remember { mutableStateOf("active") }
    val project = state.projects.firstOrNull { it.projectId == selectedProjectId }

    fun runProjectMutation(
        mutation: (String) -> ProjectMutationResult,
        successMessageResId: Int,
        onSuccess: () -> Unit = { viewModel.reload() }
    ) {
        val token = sessionManager.token() ?: return
        Thread {
            val result = mutation(token)
            mainHandler.post {
                when (result) {
                    ProjectMutationResult.Success -> {
                        Toast.makeText(context, successMessageResId, Toast.LENGTH_LONG).show()
                        onSuccess()
                        viewModel.reload()
                    }
                    ProjectMutationResult.Unauthorized -> {
                        sessionManager.clear()
                        activity.startActivity(Intent(activity, MainActivity::class.java))
                        activity.finish()
                    }
                    ProjectMutationResult.NetworkError,
                    is ProjectMutationResult.ServerError -> {
                        Toast.makeText(context, R.string.project_action_error, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }.start()
    }

    DisposableEffect(viewModel) {
        viewModel.observe { state = it }
        onDispose { }
    }

    LaunchedEffect(state.requiresLogin) {
        if (state.requiresLogin) {
            activity.startActivity(Intent(activity, MainActivity::class.java))
            activity.finish()
        }
    }

    AppScaffold(selectedDestination = Destination.PROJECTS) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .screenContentPadding()
        ) {
            if (project == null) {
                Text(
                    text = stringResource(R.string.projects_title),
                    modifier = Modifier.fillMaxWidth(),
                    color = colorResource(R.color.dashboard_text_primary),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.projects_subtitle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    color = colorResource(R.color.dashboard_text_secondary),
                    fontSize = 15.sp
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (project == null) {
                    if (state.canCreateTasks) {
                        FilledActionButton(
                            text = stringResource(R.string.project_create_project),
                            colorResId = R.color.login_button,
                            radius = 6.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                                .height(46.dp),
                            onClick = {
                                context.startActivity(Intent(context, CreateProjectActivity::class.java))
                            }
                        )
                    }
                    if (state.projects.isEmpty()) {
                        Text(
                            text = stringResource(R.string.projects_empty),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            color = colorResource(R.color.dashboard_text_secondary),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    state.projects.forEach { item ->
                        ProjectCard(
                            project = item,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp),
                            onClick = { selectedProjectId = item.projectId }
                        )
                    }
                } else {
                    BackLink(
                        text = stringResource(R.string.project_detail_back),
                        onClick = { selectedProjectId = null }
                    )
                    ProjectDetailHero(project = project)
                    if (canManage) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilledActionButton(
                                text = stringResource(R.string.project_edit),
                                colorResId = R.color.login_button,
                                radius = 6.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                onClick = {
                                    editName = project.nameText.orEmpty()
                                    editDescription = project.descriptionText.orEmpty()
                                    editStatus = project.status
                                    showEditProject = true
                                }
                            )
                            FilledActionButton(
                                text = stringResource(R.string.project_delete),
                                colorResId = R.color.settings_logout,
                                radius = 6.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                onClick = {
                                    runProjectMutation(
                                        mutation = { token -> projectsApi.deleteProject(token, project.projectId) },
                                        successMessageResId = R.string.project_deleted,
                                        onSuccess = { selectedProjectId = null }
                                    )
                                }
                            )
                        }
                    }
                    if (state.canCreateTasks) {
                        FilledActionButton(
                            text = stringResource(R.string.project_create_task),
                            colorResId = R.color.login_button,
                            radius = 8.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp, bottom = 4.dp)
                                .height(48.dp),
                            onClick = {
                                val intent = Intent(context, CreateTaskActivity::class.java)
                                intent.putExtra(CreateTaskActivity.EXTRA_PROJECT_ID, project.projectId)
                                intent.putExtra(
                                    CreateTaskActivity.EXTRA_PROJECT_NAME,
                                    project.nameText ?: context.getString(project.nameResId)
                                )
                                context.startActivity(intent)
                            }
                        )
                    }
                    SectionTitle(
                        titleResId = R.string.project_detail_tasks,
                        topPadding = 20.dp,
                        trailing = project.tasks.size.toString()
                    )
                    project.tasks.forEach { task ->
                        ProjectTaskCard(
                            projectTask = task,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(112.dp)
                                .padding(bottom = 10.dp),
                            onClick = {
                                if (task.task.taskId > 0) {
                                    val intent = Intent(context, TaskDetailActivity::class.java)
                                    intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, task.task.taskId)
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
                    SectionTitle(
                        titleResId = R.string.project_detail_people,
                        topPadding = 18.dp,
                        trailing = project.members.size.toString()
                    )
                    project.members.forEach { member ->
                        MemberRow(
                            member = member,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            onRemove = if (canManage && !member.isManager && member.userId > 0) {
                                {
                                    runProjectMutation(
                                        mutation = { token ->
                                            projectsApi.removeMember(token, project.projectId, member.userId)
                                        },
                                        successMessageResId = R.string.project_member_removed
                                    )
                                }
                            } else {
                                null
                            }
                        )
                    }
                }
            }
        }
    }

    if (showEditProject && project != null) {
        AlertDialog(
            onDismissRequest = { showEditProject = false },
            title = { Text(stringResource(R.string.project_edit)) },
            text = {
                Column {
                    CreateTaskLabelNoTop(R.string.create_project_name)
                    CreateTaskInput(value = editName, onValueChange = { editName = it }, singleLine = true)
                    CreateTaskLabel(R.string.create_project_description)
                    CreateTaskInput(value = editDescription, onValueChange = { editDescription = it }, singleLine = false)
                    CreateTaskLabel(R.string.create_task_status)
                    CreateTaskInput(value = editStatus, onValueChange = { editStatus = it }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        runProjectMutation(
                            mutation = { token ->
                                projectsApi.updateProject(
                                    token,
                                    project.projectId,
                                    UpdateProjectInput(
                                        name = editName.trim(),
                                        description = editDescription.trim(),
                                        status = editStatus.trim()
                                    )
                                )
                            },
                            successMessageResId = R.string.project_updated,
                            onSuccess = { showEditProject = false }
                        )
                    }
                ) {
                    Text(stringResource(R.string.create_project_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProject = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun ProjectDetailHero(project: Project) {
    val projectName = project.nameText ?: stringResource(project.nameResId)
    val projectDescription = project.descriptionText ?: stringResource(project.descriptionResId)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .cardBackground(R.color.dashboard_card, R.color.dashboard_card_stroke, 12.dp)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(colorResource(project.badgeColorResId)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = project.initials,
                    color = colorResource(project.accentColorResId),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
            ) {
                Text(
                    text = projectName,
                    color = colorResource(R.color.dashboard_text_primary),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                ProjectStatusChip(
                    status = project.status,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        if (projectDescription.isNotBlank()) {
            Text(
                text = projectDescription,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                color = colorResource(R.color.dashboard_text_secondary),
                fontSize = 15.sp,
                lineHeight = 21.sp
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            project.managerName?.let { managerName ->
                ProjectMetaChip(
                    label = stringResource(R.string.project_manager_label, managerName),
                    modifier = Modifier.weight(1f)
                )
            }
            if (!project.startDate.isNullOrBlank() && !project.estimatedEndDate.isNullOrBlank()) {
                ProjectMetaChip(
                    label = stringResource(R.string.project_dates, project.startDate, project.estimatedEndDate),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        project.actualEndDate?.takeIf { it.isNotBlank() }?.let { actualEnd ->
            ProjectMetaChip(
                label = stringResource(R.string.project_actual_end, actualEnd),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            )
        }
        if (project.members.isNotEmpty()) {
            AvatarStack(
                members = project.members,
                avatarSize = 34.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun ProjectStatusChip(status: String, modifier: Modifier = Modifier) {
    val normalized = status.lowercase(Locale.getDefault())
    val background = when (normalized) {
        "completed", "done" -> R.color.project_green_soft
        "blocked", "cancelled" -> R.color.task_red_soft
        "in_progress", "active" -> R.color.task_blue_soft
        else -> R.color.task_status_gray_bg
    }
    val textColor = when (normalized) {
        "completed", "done" -> R.color.project_green
        "blocked", "cancelled" -> R.color.task_red
        "in_progress", "active" -> R.color.bottom_nav_selected
        else -> R.color.task_status_gray_text
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colorResource(background))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.replace('_', ' ').replaceFirstChar { it.uppercase() },
            color = colorResource(textColor),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProjectMetaChip(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colorResource(R.color.dashboard_muted))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = colorResource(R.color.dashboard_text_secondary),
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun BackLink(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_arrow_right),
            contentDescription = null,
            modifier = Modifier
                .size(18.dp)
                .rotate(180f),
            colorFilter = ColorFilter.tint(colorResource(R.color.bottom_nav_selected))
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 6.dp),
            color = colorResource(R.color.bottom_nav_selected),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CreateProjectScreen() {
    val context = LocalContext.current
    val activity = context.findActivity()
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val projectsApi = remember { ProjectsApi() }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val currentUser = remember { sessionManager.currentUser() }
    var users by remember { mutableStateOf<List<ApiUser>>(emptyList()) }
    var usersLoaded by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var managerLabel by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var estimatedEndDate by remember { mutableStateOf("") }
    var memberIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var requiredError by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val token = sessionManager.token()
        if (token.isNullOrBlank()) {
            activity.startActivity(Intent(activity, MainActivity::class.java))
            activity.finish()
            return@LaunchedEffect
        }

        Thread {
            val result = projectsApi.users(token)
            mainHandler.post {
                when (result) {
                    is UsersResult.Success -> {
                        users = result.users
                        val preferredManager = result.users.firstOrNull { it.userId == currentUser?.userId }
                            ?: result.users.firstOrNull { it.canManageProjects() }
                        managerLabel = preferredManager?.label().orEmpty()
                    }
                    UsersResult.Unauthorized -> {
                        sessionManager.clear()
                        activity.startActivity(Intent(activity, MainActivity::class.java))
                        activity.finish()
                    }
                    UsersResult.NetworkError,
                    is UsersResult.ServerError -> {
                        managerLabel = currentUser?.name.orEmpty()
                    }
                }
                usersLoaded = true
            }
        }.start()
    }

    val managerOptions = users.filter { it.canManageProjects() }.map { it.label() }

    AppScaffold(selectedDestination = Destination.PROJECTS) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .screenContentPadding(bottom = 24.dp)
        ) {
            ScreenHeader(
                title = stringResource(R.string.create_project_title),
                subtitle = stringResource(R.string.create_project_subtitle)
            )

            FormSection(R.string.create_project_section_details) {
                CreateTaskLabelNoTop(R.string.create_project_name)
                CreateTaskInput(
                    value = name,
                    onValueChange = {
                        name = it
                        requiredError = false
                    },
                    singleLine = true,
                    keyboardType = KeyboardType.Text
                )
                CreateTaskLabel(R.string.create_project_description)
                CreateTaskInput(
                    value = description,
                    onValueChange = { description = it },
                    minHeight = 92.dp,
                    singleLine = false
                )
            }

            FormSection(R.string.create_project_section_leadership) {
                CreateTaskLabelNoTop(R.string.create_project_manager)
                if (managerOptions.isEmpty()) {
                    Text(
                        text = if (usersLoaded) {
                            stringResource(R.string.create_project_no_users)
                        } else {
                            ""
                        },
                        modifier = Modifier.padding(top = 6.dp),
                        color = colorResource(R.color.dashboard_text_secondary),
                        fontSize = 13.sp
                    )
                } else {
                    SelectInput(
                        selected = managerLabel.ifBlank { managerOptions.first() },
                        values = managerOptions,
                        onSelected = { managerLabel = it }
                    )
                }
            }

            FormSection(R.string.create_project_section_schedule) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        CreateTaskLabelNoTop(R.string.create_project_start_date)
                        PickerInput(
                            value = startDate,
                            hint = stringResource(R.string.create_task_date_hint),
                            iconResId = R.drawable.ic_calendar_clock,
                            onClick = { showDatePicker(context) { startDate = it } }
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        CreateTaskLabelNoTop(R.string.create_project_estimated_end_date)
                        PickerInput(
                            value = estimatedEndDate,
                            hint = stringResource(R.string.create_task_date_hint),
                            iconResId = R.drawable.ic_calendar_clock,
                            onClick = { showDatePicker(context) { estimatedEndDate = it } }
                        )
                    }
                }
            }

            if (users.isNotEmpty()) {
                FormSection(R.string.create_project_members) {
                    users.forEach { user ->
                        val selected = memberIds.contains(user.userId)
                        SelectableMemberRow(
                            user = user,
                            selected = selected,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .padding(top = 8.dp),
                            onClick = {
                                memberIds = if (selected) {
                                    memberIds - user.userId
                                } else {
                                    memberIds + user.userId
                                }
                            }
                        )
                    }
                }
            }

            if (requiredError) {
                Text(
                    text = stringResource(R.string.create_project_required_error),
                    modifier = Modifier.padding(top = 12.dp),
                    color = colorResource(R.color.login_error),
                    fontSize = 12.sp
                )
            }

            FilledActionButton(
                text = stringResource(R.string.create_project_save),
                colorResId = R.color.login_button,
                radius = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .height(50.dp),
                onClick = {
                    if (isSaving) {
                        return@FilledActionButton
                    }
                    if (name.trim().isEmpty() || startDate.isBlank() || estimatedEndDate.isBlank()) {
                        requiredError = true
                        return@FilledActionButton
                    }

                    val token = sessionManager.token()
                    if (token.isNullOrBlank()) {
                        activity.startActivity(Intent(activity, MainActivity::class.java))
                        activity.finish()
                        return@FilledActionButton
                    }

                    val managerId = users.firstOrNull { it.label() == managerLabel }?.userId
                        ?: currentUser?.userId
                        ?: 0
                    isSaving = true
                    Thread {
                        val result = projectsApi.createProject(
                            token,
                            CreateProjectInput(
                                name = name.trim(),
                                description = description.trim(),
                                managerId = managerId,
                                startDate = startDate,
                                estimatedEndDate = estimatedEndDate,
                                memberIds = memberIds.toList()
                            )
                        )
                        mainHandler.post {
                            isSaving = false
                            when (result) {
                                CreateProjectResult.Success -> {
                                    Toast.makeText(context, R.string.create_project_saved, Toast.LENGTH_LONG).show()
                                    activity.startActivity(Intent(activity, ProjectsActivity::class.java))
                                    activity.finish()
                                }
                                CreateProjectResult.Unauthorized -> {
                                    sessionManager.clear()
                                    activity.startActivity(Intent(activity, MainActivity::class.java))
                                    activity.finish()
                                }
                                CreateProjectResult.NetworkError,
                                is CreateProjectResult.ServerError -> {
                                    Toast.makeText(context, R.string.create_project_error, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }.start()
                }
            )
        }
    }
}

@Composable
fun CreateTaskScreen(projectId: Int, projectName: String?) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val projectsApi = remember { ProjectsApi() }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val resolvedProjectName = projectName?.takeIf { it.trim().isNotEmpty() }
        ?: stringResource(R.string.project_unnamed)
    var assignees by remember { mutableStateOf<List<ApiUser>>(emptyList()) }
    var assigneesLoaded by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var assigneeLabel by remember { mutableStateOf("") }
    var estimatedEndDate by remember { mutableStateOf("") }
    var estimatedTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var requiredError by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(projectId) {
        val token = sessionManager.token()
        if (token.isNullOrBlank() || projectId <= 0) {
            activity.startActivity(Intent(activity, MainActivity::class.java))
            activity.finish()
            return@LaunchedEffect
        }

        Thread {
            val result = projectsApi.projectUsers(token, projectId)
            mainHandler.post {
                when (result) {
                    is UsersResult.Success -> {
                        assignees = result.users
                        assigneeLabel = result.users.firstOrNull()?.label().orEmpty()
                    }
                    UsersResult.Unauthorized -> {
                        sessionManager.clear()
                        activity.startActivity(Intent(activity, MainActivity::class.java))
                        activity.finish()
                    }
                    UsersResult.NetworkError,
                    is UsersResult.ServerError -> {
                        Thread {
                            val fallback = projectsApi.users(token)
                            mainHandler.post {
                                when (fallback) {
                                    is UsersResult.Success -> {
                                        assignees = fallback.users
                                        assigneeLabel = fallback.users.firstOrNull()?.label().orEmpty()
                                    }
                                    else -> Toast.makeText(context, R.string.create_task_members_error, Toast.LENGTH_LONG).show()
                                }
                            }
                        }.start()
                    }
                }
                assigneesLoaded = true
            }
        }.start()
    }

    AppScaffold(selectedDestination = Destination.PROJECTS) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .screenContentPadding(bottom = 24.dp)
        ) {
            ScreenHeader(
                title = stringResource(R.string.create_task_title),
                subtitle = stringResource(R.string.create_task_subtitle, resolvedProjectName)
            )

            FormSection(R.string.create_task_section_details) {
                CreateTaskLabelNoTop(R.string.create_task_task_title)
                CreateTaskInput(
                    value = title,
                    onValueChange = {
                        title = it
                        requiredError = false
                    },
                    singleLine = true,
                    keyboardType = KeyboardType.Text
                )
                CreateTaskLabel(R.string.create_task_user)
                if (assignees.isEmpty()) {
                    Text(
                        text = if (assigneesLoaded) {
                            stringResource(R.string.create_task_members_error)
                        } else {
                            ""
                        },
                        modifier = Modifier.padding(top = 6.dp),
                        color = colorResource(R.color.dashboard_text_secondary),
                        fontSize = 13.sp
                    )
                } else {
                    SelectInput(
                        selected = assigneeLabel.ifBlank { assignees.first().label() },
                        values = assignees.map { it.label() },
                        onSelected = {
                            assigneeLabel = it
                            requiredError = false
                        }
                    )
                }
                CreateTaskLabel(R.string.create_task_description)
                CreateTaskInput(
                    value = description,
                    onValueChange = { description = it },
                    minHeight = 92.dp,
                    singleLine = false
                )
            }

            FormSection(R.string.create_task_section_schedule) {
                CreateTaskLabelNoTop(R.string.create_task_estimated_end_date)
                PickerInput(
                    value = estimatedEndDate,
                    hint = stringResource(R.string.create_task_date_hint),
                    iconResId = R.drawable.ic_calendar_clock,
                    onClick = { showDatePicker(context) { estimatedEndDate = it } }
                )
                CreateTaskLabel(R.string.create_task_estimated_time)
                CreateTaskInput(
                    value = estimatedTime,
                    onValueChange = { estimatedTime = it },
                    singleLine = true,
                    keyboardType = KeyboardType.Number
                )
                CreateTaskLabel(R.string.create_task_location)
                CreateTaskInput(value = location, onValueChange = { location = it }, singleLine = true)
            }

            if (requiredError) {
                Text(
                    text = stringResource(R.string.create_task_required_error),
                    modifier = Modifier.padding(top = 12.dp),
                    color = colorResource(R.color.login_error),
                    fontSize = 12.sp
                )
            }
            FilledActionButton(
                text = stringResource(R.string.create_task_save),
                colorResId = R.color.login_button,
                radius = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .height(50.dp),
                onClick = {
                    if (isSaving) {
                        return@FilledActionButton
                    }
                    val userId = assignees.firstOrNull { it.label() == assigneeLabel }?.userId ?: 0
                    val estimatedTimeValue = estimatedTime.toDoubleOrNull()
                    if (
                        projectId <= 0 ||
                        title.trim().isEmpty() ||
                        userId <= 0 ||
                        estimatedEndDate.isBlank() ||
                        estimatedTimeValue == null ||
                        estimatedTimeValue < 0
                    ) {
                        requiredError = true
                        return@FilledActionButton
                    }
                    val token = sessionManager.token()
                    if (token.isNullOrBlank()) {
                        activity.startActivity(Intent(activity, MainActivity::class.java))
                        activity.finish()
                        return@FilledActionButton
                    }

                    isSaving = true
                    Thread {
                        val result = projectsApi.createTask(
                            token,
                            projectId,
                            RemoteCreateTaskInput(
                                userId = userId,
                                title = title.trim(),
                                description = description.trim(),
                                estimatedEndDate = estimatedEndDate,
                                estimatedTime = estimatedTimeValue,
                                location = location.trim()
                            )
                        )
                        mainHandler.post {
                            isSaving = false
                            when (result) {
                                CreateTaskResult.Success -> {
                                    Toast.makeText(context, R.string.create_task_saved, Toast.LENGTH_LONG).show()
                                    activity.finish()
                                }
                                CreateTaskResult.Unauthorized -> {
                                    sessionManager.clear()
                                    activity.startActivity(Intent(activity, MainActivity::class.java))
                                    activity.finish()
                                }
                                CreateTaskResult.NetworkError,
                                is CreateTaskResult.ServerError -> {
                                    Toast.makeText(context, R.string.create_task_error, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }.start()
                }
            )
        }
    }
}

@Composable
fun TaskDetailScreen(taskId: Int) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val taskApi = remember { TaskApi() }
    val photoApi = remember { PhotoApi() }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val canManageTasks = remember { sessionManager.canManageProjects() }
    var task by remember { mutableStateOf<ApiTask?>(null) }
    var timeToAdd by remember { mutableStateOf("") }
    var workDate by remember { mutableStateOf(currentDateString()) }
    var observation by remember { mutableStateOf("") }
    var photoPath by remember { mutableStateOf("") }
    var requiredError by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    fun authFailure() {
        sessionManager.clear()
        activity.startActivity(Intent(activity, MainActivity::class.java))
        activity.finish()
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val token = sessionManager.token() ?: return@rememberLauncherForActivityResult
        if (uri == null) return@rememberLauncherForActivityResult
        Thread {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@Thread
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val result = photoApi.uploadPhoto(token, "task-photo.jpg", bytes, mimeType)
            mainHandler.post {
                when (result) {
                    is PhotoUploadResult.Success -> {
                        photoPath = result.path
                        Toast.makeText(context, R.string.task_detail_updated, Toast.LENGTH_SHORT).show()
                    }
                    PhotoUploadResult.Unauthorized -> authFailure()
                    PhotoUploadResult.NetworkError,
                    is PhotoUploadResult.ServerError -> {
                        Toast.makeText(context, R.string.task_detail_error, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }.start()
    }

    fun loadTask() {
        val token = sessionManager.token()
        if (token.isNullOrBlank() || taskId <= 0) {
            authFailure()
            return
        }
        Thread {
            val result = taskApi.getTask(token, taskId)
            mainHandler.post {
                when (result) {
                    is TaskResult.Success -> {
                        task = result.task
                        if (observation.isBlank()) {
                            observation = result.task.observation
                        }
                    }
                    TaskResult.Unauthorized -> authFailure()
                    TaskResult.NetworkError,
                    is TaskResult.ServerError -> Toast.makeText(context, R.string.task_detail_error, Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    fun mutate(action: (String) -> TaskMutationResult, successMessage: Int, afterSuccess: () -> Unit = { loadTask() }) {
        val token = sessionManager.token()
        if (token.isNullOrBlank()) {
            authFailure()
            return
        }
        isSaving = true
        Thread {
            val result = action(token)
            mainHandler.post {
                isSaving = false
                when (result) {
                    TaskMutationResult.Success -> {
                        Toast.makeText(context, successMessage, Toast.LENGTH_LONG).show()
                        afterSuccess()
                    }
                    TaskMutationResult.Unauthorized -> authFailure()
                    TaskMutationResult.NetworkError,
                    is TaskMutationResult.ServerError -> Toast.makeText(context, R.string.task_detail_error, Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    LaunchedEffect(taskId) {
        loadTask()
    }

    AppScaffold(selectedDestination = Destination.PROJECTS) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .screenContentPadding(bottom = 24.dp)
        ) {
            Text(
                text = task?.title ?: stringResource(R.string.task_detail_title),
                modifier = Modifier.fillMaxWidth(),
                color = colorResource(R.color.dashboard_text_primary),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = taskStatusLabel(task?.status).uppercase(Locale.getDefault()),
                modifier = Modifier.padding(top = 4.dp),
                color = colorResource(R.color.dashboard_text_secondary),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            SettingsPanel(Modifier.padding(top = 22.dp)) {
                TaskDetailField(R.string.task_detail_description, task?.description.orEmpty())
                TaskDetailField(R.string.task_detail_status, taskStatusLabel(task?.status))
                TaskDetailField(R.string.task_detail_estimated_end_date, task?.estimatedEndDate?.take(10).orEmpty())
                TaskDetailField(R.string.task_detail_actual_end_date, task?.actualEndDate?.take(10).orEmpty())
                TaskDetailField(R.string.task_detail_estimated_time, task?.estimatedTime?.toString().orEmpty())
                TaskDetailField(R.string.task_detail_time_spent, task?.timeSpent?.toString().orEmpty())
                TaskDetailField(R.string.task_detail_work_date, task?.workDate?.take(10).orEmpty())
                TaskDetailField(R.string.task_detail_location, task?.location.orEmpty())
                TaskDetailField(R.string.task_detail_observation, task?.observation.orEmpty())
                TaskDetailField(
                    R.string.task_detail_photo,
                    when {
                        photoPath.isNotBlank() -> photoApi.photoUrl(photoPath)
                        !task?.photo.isNullOrBlank() -> photoApi.photoUrl(task?.photo.orEmpty())
                        else -> ""
                    }
                )
            }

            FilledActionButton(
                text = stringResource(R.string.task_detail_upload_photo),
                colorResId = R.color.login_button,
                radius = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(42.dp),
                onClick = { photoPicker.launch("image/*") }
            )

            SettingsPanel(Modifier.padding(top = 16.dp)) {
                Text(
                    text = stringResource(R.string.task_detail_add_time).uppercase(Locale.getDefault()),
                    modifier = Modifier.fillMaxWidth(),
                    color = colorResource(R.color.dashboard_text_secondary),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        CreateTaskLabelNoTop(R.string.task_detail_time_spent)
                        CreateTaskInput(
                            value = timeToAdd,
                            onValueChange = {
                                timeToAdd = it
                                requiredError = false
                            },
                            singleLine = true,
                            keyboardType = KeyboardType.Number
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        CreateTaskLabelNoTop(R.string.task_detail_work_date)
                        PickerInput(
                            value = workDate,
                            hint = stringResource(R.string.create_task_date_hint),
                            iconResId = R.drawable.ic_calendar_clock,
                            onClick = { showDatePicker(context) { workDate = it } }
                        )
                    }
                }
                CreateTaskLabel(R.string.task_detail_observation)
                CreateTaskInput(
                    value = observation,
                    onValueChange = { observation = it },
                    minHeight = 82.dp,
                    singleLine = false
                )
                if (requiredError) {
                    Text(
                        text = stringResource(R.string.create_task_required_error),
                        modifier = Modifier.padding(top = 12.dp),
                        color = colorResource(R.color.login_error),
                        fontSize = 12.sp
                    )
                }
                FilledActionButton(
                    text = stringResource(R.string.task_detail_add_time),
                    colorResId = R.color.login_button,
                    radius = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp)
                        .height(46.dp),
                    onClick = {
                        if (isSaving) return@FilledActionButton
                        val time = timeToAdd.toDoubleOrNull()
                        if (time == null || time <= 0 || workDate.isBlank()) {
                            requiredError = true
                            return@FilledActionButton
                        }
                        mutate(
                            action = { token -> taskApi.addTimeSpent(token, taskId, time, workDate, observation.trim()) },
                            successMessage = R.string.task_detail_updated,
                            afterSuccess = {
                                timeToAdd = ""
                                loadTask()
                            }
                        )
                    }
                )
            }

            FilledActionButton(
                text = stringResource(R.string.task_detail_complete),
                colorResId = R.color.project_green,
                radius = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .height(46.dp),
                onClick = {
                    if (isSaving) return@FilledActionButton
                    mutate(
                        action = { token ->
                            taskApi.complete(
                                token,
                                taskId,
                                workDate,
                                observation.trim(),
                                photo = photoPath.ifBlank { task?.photo },
                                location = task?.location
                            )
                        },
                        successMessage = R.string.task_detail_updated
                    )
                }
            )
            if (canManageTasks) {
                FilledActionButton(
                    text = stringResource(R.string.task_detail_delete),
                    colorResId = R.color.settings_logout,
                    radius = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .height(46.dp),
                    onClick = {
                        if (isSaving) return@FilledActionButton
                        mutate(
                            action = { token -> taskApi.delete(token, taskId) },
                            successMessage = R.string.task_detail_deleted,
                            afterSuccess = { activity.finish() }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun AdminScreen() {
    val context = LocalContext.current
    val activity = context.findActivity()
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val adminApi = remember { AdminApi() }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val currentUser = remember { sessionManager.currentUser() }
    var users by remember { mutableStateOf<List<ApiUser>>(emptyList()) }
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("user") }
    var active by remember { mutableStateOf(true) }
    var editingUserId by remember { mutableStateOf<Int?>(null) }
    var requiredError by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    fun clearForm() {
        editingUserId = null
        name = ""
        username = ""
        email = ""
        password = ""
        role = "user"
        active = true
    }

    fun loadUserIntoForm(user: ApiUser) {
        editingUserId = user.userId
        name = user.name
        username = user.username
        email = user.email
        password = ""
        role = user.role.ifBlank { "user" }
        active = user.active
    }

    fun handleAuthFailure() {
        sessionManager.clear()
        activity.startActivity(Intent(activity, MainActivity::class.java))
        activity.finish()
    }

    fun loadUsers() {
        val token = sessionManager.token()
        if (token.isNullOrBlank()) {
            handleAuthFailure()
            return
        }

        Thread {
            val result = adminApi.users(token)
            mainHandler.post {
                when (result) {
                    is AdminUsersResult.Success -> users = result.users
                    AdminUsersResult.Unauthorized -> handleAuthFailure()
                    AdminUsersResult.Forbidden -> {
                        Toast.makeText(context, R.string.admin_forbidden, Toast.LENGTH_LONG).show()
                        activity.startActivity(Intent(activity, DashboardActivity::class.java))
                        activity.finish()
                    }
                    AdminUsersResult.NetworkError,
                    is AdminUsersResult.ServerError -> {
                        Toast.makeText(context, R.string.admin_error, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }.start()
    }

    fun mutate(
        mutation: (String) -> AdminMutationResult,
        successMessageResId: Int,
        onSuccess: () -> Unit = {},
        onFinished: () -> Unit = {}
    ) {
        val token = sessionManager.token()
        if (token.isNullOrBlank()) {
            handleAuthFailure()
            onFinished()
            return
        }

        Thread {
            val result = mutation(token)
            mainHandler.post {
                when (result) {
                    AdminMutationResult.Success -> {
                        Toast.makeText(context, successMessageResId, Toast.LENGTH_LONG).show()
                        onSuccess()
                        loadUsers()
                    }
                    AdminMutationResult.Unauthorized -> handleAuthFailure()
                    AdminMutationResult.Forbidden -> Toast.makeText(context, R.string.admin_forbidden, Toast.LENGTH_LONG).show()
                    AdminMutationResult.NetworkError,
                    is AdminMutationResult.ServerError -> Toast.makeText(context, R.string.admin_error, Toast.LENGTH_LONG).show()
                }
                onFinished()
            }
        }.start()
    }

    LaunchedEffect(Unit) {
        if (!sessionManager.isAdmin()) {
            Toast.makeText(context, R.string.admin_forbidden, Toast.LENGTH_LONG).show()
            activity.startActivity(Intent(activity, DashboardActivity::class.java))
            activity.finish()
            return@LaunchedEffect
        }
        loadUsers()
    }

    val adminCount = users.count { it.role == "admin" }
    val managerCount = users.count { it.role == "project_manager" }
    val roleUserLabel = stringResource(R.string.admin_role_user)
    val roleManagerLabel = stringResource(R.string.admin_role_manager)
    val roleAdminLabel = stringResource(R.string.admin_role_admin)

    AppScaffold(selectedDestination = Destination.ADMIN) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .screenContentPadding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.admin_title),
                modifier = Modifier.fillMaxWidth(),
                color = colorResource(R.color.dashboard_text_primary),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.admin_subtitle),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                color = colorResource(R.color.dashboard_text_secondary),
                fontSize = 15.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp)
                    .height(86.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminMetricCard(
                    label = stringResource(R.string.admin_users),
                    value = users.size.toString(),
                    colorResId = R.color.bottom_nav_selected,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    label = stringResource(R.string.admin_metric_admins),
                    value = adminCount.toString(),
                    colorResId = R.color.project_purple,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    label = stringResource(R.string.admin_metric_managers),
                    value = managerCount.toString(),
                    colorResId = R.color.project_green,
                    modifier = Modifier.weight(1f)
                )
            }

            SettingsPanel(Modifier.padding(top = 16.dp)) {
                Text(
                    text = stringResource(R.string.admin_create_user).uppercase(Locale.getDefault()),
                    modifier = Modifier.fillMaxWidth(),
                    color = colorResource(R.color.dashboard_text_secondary),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        CreateTaskLabelNoTop(R.string.admin_name)
                        CreateTaskInput(
                            value = name,
                            onValueChange = {
                                name = it
                                requiredError = false
                            },
                            singleLine = true
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        CreateTaskLabelNoTop(R.string.admin_username)
                        CreateTaskInput(
                            value = username,
                            onValueChange = {
                                username = it
                                requiredError = false
                            },
                            singleLine = true
                        )
                    }
                }
                CreateTaskLabel(R.string.admin_email)
                CreateTaskInput(
                    value = email,
                    onValueChange = {
                        email = it
                        requiredError = false
                    },
                    singleLine = true,
                    keyboardType = KeyboardType.Email
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        CreateTaskLabelNoTop(R.string.admin_password)
                        CreateTaskInput(
                            value = password,
                            onValueChange = {
                                password = it
                                requiredError = false
                            },
                            singleLine = true,
                            keyboardType = KeyboardType.Password,
                            visualTransformation = PasswordVisualTransformation()
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        CreateTaskLabelNoTop(R.string.admin_role)
                        SelectInput(
                            selected = roleLabel(role),
                            values = listOf(roleUserLabel, roleManagerLabel, roleAdminLabel),
                            onSelected = { selected ->
                                role = when (selected) {
                                    roleAdminLabel -> "admin"
                                    roleManagerLabel -> "project_manager"
                                    else -> "user"
                                }
                            }
                        )
                    }
                }
                if (requiredError) {
                    Text(
                        text = stringResource(R.string.admin_required_error),
                        modifier = Modifier.padding(top = 12.dp),
                        color = colorResource(R.color.login_error),
                        fontSize = 12.sp
                    )
                }
                if (editingUserId != null) {
                    FilledActionButton(
                        text = stringResource(R.string.admin_cancel_edit),
                        colorResId = R.color.task_status_gray_text,
                        radius = 6.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .height(42.dp),
                        onClick = { clearForm() }
                    )
                }
                FilledActionButton(
                    text = stringResource(
                        if (editingUserId == null) R.string.admin_save_user else R.string.admin_update_user
                    ),
                    colorResId = R.color.login_button,
                    radius = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp)
                        .height(46.dp),
                    onClick = {
                        if (isSaving) {
                            return@FilledActionButton
                        }
                        val isEdit = editingUserId != null
                        if (
                            name.trim().isEmpty() ||
                            username.trim().isEmpty() ||
                            email.trim().isEmpty() ||
                            (!isEdit && password.length < 6)
                        ) {
                            requiredError = true
                            return@FilledActionButton
                        }
                        isSaving = true
                        val userId = editingUserId
                        mutate(
                            mutation = { token ->
                                if (userId == null) {
                                    adminApi.createUser(
                                        token,
                                        CreateUserInput(
                                            name = name.trim(),
                                            username = username.trim(),
                                            email = email.trim(),
                                            password = password,
                                            role = role,
                                            active = active
                                        )
                                    )
                                } else {
                                    adminApi.updateUser(
                                        token,
                                        userId,
                                        UpdateUserInput(
                                            name = name.trim(),
                                            username = username.trim(),
                                            email = email.trim(),
                                            password = password.takeIf { it.length >= 6 },
                                            role = role,
                                            active = active
                                        )
                                    )
                                }
                            },
                            successMessageResId = if (userId == null) {
                                R.string.admin_create_success
                            } else {
                                R.string.admin_update_success
                            },
                            onSuccess = { clearForm() },
                            onFinished = { isSaving = false }
                        )
                    }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.admin_users),
                    modifier = Modifier.weight(1f),
                    color = colorResource(R.color.dashboard_text_primary),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = users.size.toString(),
                    color = colorResource(R.color.dashboard_text_secondary),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            users.forEach { user ->
                AdminUserRow(
                    user = user,
                    canDelete = user.userId != currentUser?.userId,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .clickable { loadUserIntoForm(user) },
                    onDelete = {
                        mutate(
                            mutation = { token -> adminApi.deleteUser(token, user.userId) },
                            successMessageResId = R.string.admin_delete_success,
                            onSuccess = {
                                if (editingUserId == user.userId) {
                                    clearForm()
                                }
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val viewModel = remember { SettingsViewModel() }
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val authApi = remember { AuthApi() }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val currentUser = remember { sessionManager.currentUser() }
    var language by remember { mutableStateOf(viewModel.getState(context).language) }
    val englishSelected = LocaleHelper.LANGUAGE_ENGLISH == language
    val currentLanguageName = stringResource(
        if (englishSelected) R.string.language_english else R.string.language_portuguese
    )

    AppScaffold(selectedDestination = Destination.SETTINGS) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .screenContentPadding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                modifier = Modifier.fillMaxWidth(),
                color = colorResource(R.color.dashboard_text_primary),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.settings_subtitle),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                color = colorResource(R.color.dashboard_text_secondary),
                fontSize = 15.sp
            )
            SettingsPanel(Modifier.padding(top = 24.dp)) {
                Text(
                    text = stringResource(R.string.settings_account).uppercase(Locale.getDefault()),
                    modifier = Modifier.fillMaxWidth(),
                    color = colorResource(R.color.dashboard_text_secondary),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentUser?.name ?: stringResource(R.string.settings_unknown_user),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    color = colorResource(R.color.dashboard_text_primary),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentUser?.email.orEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    color = colorResource(R.color.dashboard_text_secondary),
                    fontSize = 14.sp
                )
            }
            SettingsPanel(Modifier.padding(top = 14.dp)) {
                Text(
                    text = stringResource(R.string.settings_language),
                    color = colorResource(R.color.dashboard_text_primary),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.settings_language_description),
                    modifier = Modifier.padding(top = 4.dp),
                    color = colorResource(R.color.dashboard_text_secondary),
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.settings_language_current, currentLanguageName),
                    modifier = Modifier.padding(top = 10.dp),
                    color = colorResource(R.color.dashboard_text_secondary),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LanguageButton(
                        text = stringResource(R.string.language_english),
                        selected = englishSelected,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (viewModel.changeLanguage(context, LocaleHelper.LANGUAGE_ENGLISH)) {
                                restartRoot(context, SettingsActivity::class.java)
                            } else {
                                language = viewModel.getState(context).language
                            }
                        }
                    )
                    LanguageButton(
                        text = stringResource(R.string.language_portuguese),
                        selected = !englishSelected,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (viewModel.changeLanguage(context, LocaleHelper.LANGUAGE_PORTUGUESE)) {
                                restartRoot(context, SettingsActivity::class.java)
                            } else {
                                language = viewModel.getState(context).language
                            }
                        }
                    )
                }
            }
            SettingsPanel(Modifier.padding(top = 14.dp)) {
                Text(
                    text = stringResource(R.string.settings_logout),
                    color = colorResource(R.color.dashboard_text_primary),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.settings_logout_description),
                    modifier = Modifier.padding(top = 4.dp),
                    color = colorResource(R.color.dashboard_text_secondary),
                    fontSize = 14.sp
                )
                FilledActionButton(
                    text = stringResource(R.string.settings_logout),
                    iconResId = R.drawable.ic_logout,
                    colorResId = R.color.settings_logout,
                    radius = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .height(46.dp),
                    onClick = {
                        val token = sessionManager.token()
                        Thread {
                            if (!token.isNullOrBlank()) {
                                authApi.logout(token)
                            }
                            sessionManager.clear()
                            mainHandler.post {
                                restartRoot(context, MainActivity::class.java)
                            }
                        }.start()
                    }
                )
            }
        }
    }
}

@Composable
private fun AppScaffold(
    selectedDestination: Destination,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.dashboard_background))
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .statusBarsPadding()
        ) {
            content()
        }
        BottomNavigation(selectedDestination)
    }
}

@Composable
private fun BottomNavigation(selectedDestination: Destination) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(72.dp)
            .background(colorResource(R.color.bottom_nav_border))
            .padding(top = 1.dp)
            .background(colorResource(R.color.bottom_nav_background))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavigationItem(
            labelResId = R.string.nav_home,
            iconResId = R.drawable.ic_home,
            selected = selectedDestination == Destination.HOME,
            modifier = Modifier.weight(1f),
            onClick = { navigate(context, selectedDestination, Destination.HOME, DashboardActivity::class.java) }
        )
        BottomNavigationItem(
            labelResId = R.string.nav_projects,
            iconResId = R.drawable.ic_projects,
            selected = selectedDestination == Destination.PROJECTS,
            modifier = Modifier.weight(1f),
            onClick = { navigate(context, selectedDestination, Destination.PROJECTS, ProjectsActivity::class.java) }
        )
        if (SessionManager(context.applicationContext).isAdmin()) {
            BottomNavigationItem(
                labelResId = R.string.nav_admin,
                iconResId = R.drawable.ic_settings,
                selected = selectedDestination == Destination.ADMIN,
                modifier = Modifier.weight(1f),
                onClick = { navigate(context, selectedDestination, Destination.ADMIN, AdminActivity::class.java) }
            )
        }
        BottomNavigationItem(
            labelResId = R.string.nav_settings,
            iconResId = R.drawable.ic_settings,
            selected = selectedDestination == Destination.SETTINGS,
            modifier = Modifier.weight(1f),
            onClick = { navigate(context, selectedDestination, Destination.SETTINGS, SettingsActivity::class.java) }
        )
    }
}

@Composable
private fun BottomNavigationItem(
    @StringRes labelResId: Int,
    @DrawableRes iconResId: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val color = colorResource(if (selected) R.color.bottom_nav_selected else R.color.bottom_nav_unselected)
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(iconResId),
            contentDescription = stringResource(labelResId),
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(color)
        )
        Text(
            text = stringResource(labelResId),
            modifier = Modifier.padding(top = 4.dp),
            color = color,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun LanguageSelector(
    modifier: Modifier = Modifier,
    onLanguageSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val languageCode = if (LocaleHelper.getSavedLanguage(context) == LocaleHelper.LANGUAGE_ENGLISH) {
        "EN"
    } else {
        "PT"
    }
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .height(32.dp)
                .clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(painterResource(R.drawable.ic_globe), contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                text = languageCode,
                modifier = Modifier.padding(start = 4.dp),
                color = colorResource(R.color.login_text_secondary),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Image(
                painterResource(R.drawable.ic_chevron_down),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 2.dp)
                    .size(16.dp)
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.language_english)) },
                onClick = {
                    expanded = false
                    onLanguageSelected(LocaleHelper.LANGUAGE_ENGLISH)
                }
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.language_portuguese)) },
                onClick = {
                    expanded = false
                    onLanguageSelected(LocaleHelper.LANGUAGE_PORTUGUESE)
                }
            )
        }
    }
}

@Composable
private fun LoginInput(
    value: String,
    onValueChange: (String) -> Unit,
    @DrawableRes iconResId: Int,
    contentDescription: String,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .inputBackground()
            .padding(start = 10.dp, end = if (trailingIcon == null) 12.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(iconResId),
            contentDescription = contentDescription,
            modifier = Modifier.size(22.dp)
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
            singleLine = true,
            textStyle = TextStyle(color = colorResource(R.color.login_text_primary), fontSize = 16.sp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation
        )
        trailingIcon?.invoke()
    }
}

@Composable
private fun ErrorText(@StringRes messageResId: Int) {
    if (messageResId != 0) {
        Text(
            text = stringResource(messageResId),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            color = colorResource(R.color.login_error),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun DashboardSummaryCard(
    @StringRes titleResId: Int,
    count: Int,
    progress: Int,
    @DrawableRes iconResId: Int,
    @ColorRes progressColorResId: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .cardBackground(R.color.dashboard_card, R.color.dashboard_card_stroke, 10.dp)
            .padding(13.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(titleResId).uppercase(Locale.getDefault()),
                modifier = Modifier.weight(1f),
                color = colorResource(R.color.dashboard_text_secondary),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Image(painterResource(iconResId), contentDescription = null, modifier = Modifier.size(22.dp))
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            Text(
                text = count.toString(),
                color = colorResource(R.color.dashboard_text_primary),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }
        ProgressLine(progress = progress, colorResId = progressColorResId)
    }
}

@Composable
private fun AdminMetricCard(
    label: String,
    value: String,
    @ColorRes colorResId: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .cardBackground(R.color.dashboard_card, R.color.dashboard_card_stroke, 8.dp)
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(colorResource(colorResId))
        )
        Text(
            text = value,
            modifier = Modifier.padding(top = 8.dp),
            color = colorResource(R.color.dashboard_text_primary),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = label,
            modifier = Modifier.padding(top = 2.dp),
            color = colorResource(R.color.dashboard_text_secondary),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProgressLine(progress: Int, @ColorRes colorResId: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(colorResource(R.color.dashboard_muted))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth((progress.coerceIn(0, 100)) / 100f)
                .fillMaxHeight()
                .background(colorResource(colorResId))
        )
    }
}

@Composable
private fun DashboardTaskCard(
    task: DashboardTask,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .cardBackground(R.color.dashboard_card, R.color.dashboard_card_stroke, 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(colorResource(task.accentColorResId))
        )
        IconBubble(task.iconBackgroundColorResId, task.iconResId, size = 30.dp, iconSize = 16.dp, radius = 15.dp)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp, end = 8.dp)
        ) {
            Text(
                text = task.titleText ?: stringResource(task.titleResId),
                color = colorResource(R.color.dashboard_text_primary),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = task.metaText ?: stringResource(task.metaResId),
                modifier = Modifier.padding(top = 2.dp),
                color = colorResource(R.color.dashboard_text_secondary),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        StatusBadge(task, height = 20.dp, minWidth = 52.dp, modifier = Modifier.padding(end = 10.dp))
    }
}

@Composable
private fun ProjectCard(
    project: Project,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .cardBackground(R.color.dashboard_card, R.color.dashboard_card_stroke, 10.dp)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(21.dp))
                    .background(colorResource(project.badgeColorResId)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = project.initials,
                    color = colorResource(project.accentColorResId),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = project.nameText ?: stringResource(project.nameResId),
                    color = colorResource(R.color.dashboard_text_primary),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.project_tasks_count, project.taskCount),
                    modifier = Modifier.padding(top = 2.dp),
                    color = colorResource(R.color.dashboard_text_secondary),
                    fontSize = 13.sp
                )
                Text(
                    text = stringResource(R.string.project_status, project.status),
                    modifier = Modifier.padding(top = 2.dp),
                    color = colorResource(R.color.dashboard_text_secondary),
                    fontSize = 12.sp
                )
            }
        }
        Text(
            text = project.descriptionText ?: stringResource(project.descriptionResId),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            color = colorResource(R.color.dashboard_text_secondary),
            fontSize = 14.sp,
            lineHeight = 16.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        AvatarStack(
            members = project.members,
            avatarSize = 32.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .padding(top = 14.dp)
        )
    }
}

@Composable
private fun ProjectTaskCard(
    projectTask: ProjectTask,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val task = projectTask.task
    Column(
        modifier = modifier
            .cardBackground(R.color.dashboard_card, R.color.dashboard_card_stroke, 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBubble(task.iconBackgroundColorResId, task.iconResId, size = 34.dp, iconSize = 18.dp, radius = 17.dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp)
            ) {
                Text(
                    text = task.titleText ?: stringResource(task.titleResId),
                    color = colorResource(R.color.dashboard_text_primary),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = task.metaText ?: stringResource(task.metaResId),
                    color = colorResource(R.color.dashboard_text_secondary),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            StatusBadge(task, height = 22.dp, minWidth = 58.dp)
        }
        AvatarStack(
            members = projectTask.assignees,
            avatarSize = 34.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(top = 14.dp)
        )
    }
}

@Composable
private fun MemberRow(
    member: ProjectMember,
    modifier: Modifier = Modifier,
    onRemove: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .height(58.dp)
            .cardBackground(R.color.dashboard_card, R.color.dashboard_card_stroke, 8.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(member = member, size = 34.dp)
        Text(
            text = member.name,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            color = colorResource(R.color.dashboard_text_primary),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        if (onRemove != null) {
            FilledActionButton(
                text = stringResource(R.string.project_remove_member),
                colorResId = R.color.settings_logout,
                radius = 6.dp,
                modifier = Modifier
                    .width(92.dp)
                    .height(34.dp),
                onClick = onRemove
            )
        }
    }
}

@Composable
private fun SelectableMemberRow(
    user: ApiUser,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val background = colorResource(if (selected) R.color.task_blue_soft else R.color.dashboard_card)
    val stroke = colorResource(if (selected) R.color.bottom_nav_selected else R.color.dashboard_card_stroke)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .border(1.dp, stroke, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = user.name,
            modifier = Modifier.weight(1f),
            color = colorResource(R.color.dashboard_text_primary),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = user.role,
            color = colorResource(R.color.dashboard_text_secondary),
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun AdminUserRow(
    user: ApiUser,
    canDelete: Boolean,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit
) {
    Row(
        modifier = modifier
            .cardBackground(R.color.dashboard_card, R.color.dashboard_card_stroke, 8.dp)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(colorResource(user.roleSoftColorResId())),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.name
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .joinToString("") { it.first().uppercase() }
                    .ifBlank { "U" },
                color = colorResource(user.roleColorResId()),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 10.dp)
        ) {
            Text(
                text = user.name,
                color = colorResource(R.color.dashboard_text_primary),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = user.email,
                modifier = Modifier.padding(top = 2.dp),
                color = colorResource(R.color.dashboard_text_secondary),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            RoleBadge(user.role, Modifier.padding(top = 7.dp))
            if (!user.active) {
                Text(
                    text = stringResource(R.string.admin_user_inactive),
                    modifier = Modifier.padding(top = 4.dp),
                    color = colorResource(R.color.settings_logout),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (canDelete) {
            FilledActionButton(
                text = stringResource(R.string.admin_delete_user),
                colorResId = R.color.settings_logout,
                radius = 6.dp,
                modifier = Modifier
                    .width(86.dp)
                    .height(36.dp),
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun TaskDetailField(@StringRes labelResId: Int, value: String) {
    Text(
        text = stringResource(labelResId).uppercase(Locale.getDefault()),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        color = colorResource(R.color.dashboard_text_secondary),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = value.ifBlank { "-" },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 3.dp),
        color = colorResource(R.color.dashboard_text_primary),
        fontSize = 14.sp
    )
}

@Composable
private fun RoleBadge(role: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colorResource(roleSoftColorResId(role)))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = roleLabel(role),
            color = colorResource(roleColorResId(role)),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

private fun ApiUser.label(): String {
    return "$name ($email)"
}

private fun ApiUser.canManageProjects(): Boolean {
    return role == "admin" || role == "project_manager"
}

@Composable
private fun roleLabel(role: String): String {
    return when (role) {
        "admin" -> stringResource(R.string.admin_role_admin)
        "project_manager" -> stringResource(R.string.admin_role_manager)
        else -> stringResource(R.string.admin_role_user)
    }
}

@Composable
private fun taskStatusLabel(statusKey: String?): String {
    return when (statusKey?.lowercase(Locale.getDefault())) {
        "completed" -> stringResource(R.string.task_status_completed)
        "blocked" -> stringResource(R.string.task_status_blocked)
        "in_progress" -> stringResource(R.string.task_status_in_progress)
        else -> stringResource(R.string.task_status_pending)
    }
}

private fun roleColorResId(role: String): Int {
    return when (role) {
        "admin" -> R.color.project_purple
        "project_manager" -> R.color.project_green
        else -> R.color.task_status_gray_text
    }
}

private fun roleSoftColorResId(role: String): Int {
    return when (role) {
        "admin" -> R.color.project_purple_soft
        "project_manager" -> R.color.project_green_soft
        else -> R.color.task_status_gray_bg
    }
}

private fun ApiUser.roleColorResId(): Int = roleColorResId(role)

private fun ApiUser.roleSoftColorResId(): Int = roleSoftColorResId(role)

private fun currentDateString(): String {
    val calendar = Calendar.getInstance()
    return String.format(
        Locale.US,
        "%04d-%02d-%02d",
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}

@Composable
private fun AvatarStack(
    members: List<ProjectMember>,
    avatarSize: Dp,
    modifier: Modifier = Modifier
) {
    val step = avatarSize * 0.75f
    Box(modifier = modifier) {
        members.forEachIndexed { index, member ->
            Avatar(
                member = member,
                size = avatarSize,
                modifier = Modifier.offset(x = step * index)
            )
        }
    }
}

@Composable
private fun Avatar(member: ProjectMember, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(colorResource(member.avatarColorResId))
            .border(2.dp, colorResource(R.color.white), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = member.initials,
            color = colorResource(R.color.white),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun IconBubble(
    @ColorRes backgroundColorResId: Int,
    @DrawableRes iconResId: Int,
    size: Dp,
    iconSize: Dp,
    radius: Dp
) {
    Box(
        modifier = Modifier
            .padding(start = 12.dp)
            .size(size)
            .clip(RoundedCornerShape(radius))
            .background(colorResource(backgroundColorResId)),
        contentAlignment = Alignment.Center
    ) {
        Image(painterResource(iconResId), contentDescription = null, modifier = Modifier.size(iconSize))
    }
}

@Composable
private fun StatusBadge(task: DashboardTask, height: Dp, minWidth: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(height)
            .widthIn(min = minWidth)
            .clip(RoundedCornerShape(14.dp))
            .background(colorResource(task.statusBackgroundColorResId))
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = taskStatusLabel(task.statusText).uppercase(Locale.getDefault()),
            color = colorResource(task.statusTextColorResId),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun SectionTitle(
    @StringRes titleResId: Int,
    topPadding: Dp,
    trailing: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(titleResId),
            modifier = Modifier.weight(1f),
            color = colorResource(R.color.dashboard_text_primary),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        if (!trailing.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colorResource(R.color.dashboard_muted))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = trailing,
                    color = colorResource(R.color.dashboard_text_secondary),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CreateTaskLabel(@StringRes textResId: Int) {
    Text(
        text = stringResource(textResId),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        color = colorResource(R.color.dashboard_text_secondary),
        fontSize = 14.sp
    )
}

@Composable
private fun CreateTaskLabelNoTop(@StringRes textResId: Int) {
    Text(
        text = stringResource(textResId),
        modifier = Modifier.fillMaxWidth(),
        color = colorResource(R.color.dashboard_text_secondary),
        fontSize = 14.sp
    )
}

@Composable
private fun CreateTaskInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    minHeight: Dp = 48.dp,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(minHeight)
            .padding(top = 6.dp)
            .inputBackground()
            .padding(horizontal = 12.dp),
        contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (singleLine) 0.dp else 12.dp),
            singleLine = singleLine,
            textStyle = TextStyle(color = colorResource(R.color.dashboard_text_primary), fontSize = 15.sp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation
        )
    }
}

@Composable
private fun SelectInput(
    selected: String,
    values: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .padding(top = 6.dp)
            .inputBackground()
            .clickable { expanded = true }
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(text = selected, color = colorResource(R.color.dashboard_text_primary), fontSize = 15.sp)
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            values.forEach { value ->
                DropdownMenuItem(
                    text = { Text(value) },
                    onClick = {
                        expanded = false
                        onSelected(value)
                    }
                )
            }
        }
    }
}

@Composable
private fun PickerInput(
    value: String,
    hint: String,
    @DrawableRes iconResId: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .padding(top = 6.dp)
            .inputBackground()
            .clickable(onClick = onClick)
            .padding(start = 12.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = value.ifEmpty { hint },
            modifier = Modifier.weight(1f),
            color = colorResource(if (value.isEmpty()) R.color.login_icon else R.color.dashboard_text_primary),
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Image(painterResource(iconResId), contentDescription = null, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingsPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .cardBackground(R.color.dashboard_card, R.color.dashboard_card_stroke, 8.dp)
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun LanguageButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val background = colorResource(if (selected) R.color.task_blue_soft else R.color.dashboard_card)
    val stroke = colorResource(if (selected) R.color.bottom_nav_selected else R.color.dashboard_card_stroke)
    val textColor = colorResource(if (selected) R.color.bottom_nav_selected else R.color.dashboard_text_primary)
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .border(1.dp, stroke, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FilledActionButton(
    text: String,
    @ColorRes colorResId: Int,
    radius: Dp,
    modifier: Modifier = Modifier,
    @DrawableRes iconResId: Int? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(radius))
            .background(colorResource(colorResId))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = text, color = colorResource(R.color.white), fontSize = 16.sp)
        if (iconResId != null) {
            Image(
                painter = painterResource(iconResId),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(18.dp)
            )
        }
    }
}

@Composable
private fun IconButtonLike(
    @DrawableRes iconResId: Int,
    contentDescription: String,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(modifier = modifier.clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(iconResId),
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            colorFilter = if (tint == Color.Unspecified) null else ColorFilter.tint(tint)
        )
    }
}

@Composable
private fun Modifier.screenContentPadding(bottom: Dp = 20.dp): Modifier {
    return padding(start = 18.dp, top = 12.dp, end = 18.dp, bottom = bottom)
}

@Composable
private fun ScreenHeader(
    title: String,
    subtitle: String? = null
) {
    Text(
        text = title,
        modifier = Modifier.fillMaxWidth(),
        color = colorResource(R.color.dashboard_text_primary),
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold
    )
    if (!subtitle.isNullOrBlank()) {
        Text(
            text = subtitle,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            color = colorResource(R.color.dashboard_text_secondary),
            fontSize = 15.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun FormSection(
    @StringRes titleResId: Int,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth().padding(top = 18.dp)) {
        Text(
            text = stringResource(titleResId).uppercase(Locale.getDefault()),
            modifier = Modifier.fillMaxWidth(),
            color = colorResource(R.color.dashboard_text_secondary),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
        SettingsPanel(Modifier.padding(top = 8.dp), content = content)
    }
}

@Composable
private fun Modifier.cardBackground(@ColorRes fill: Int, @ColorRes stroke: Int, radius: Dp): Modifier {
    val shape = RoundedCornerShape(radius)
    return clip(shape)
        .background(colorResource(fill))
        .border(1.dp, colorResource(stroke), shape)
}

@Composable
private fun Modifier.inputBackground(): Modifier {
    val shape = RoundedCornerShape(8.dp)
    return clip(shape)
        .background(colorResource(R.color.login_input_background))
        .border(1.dp, colorResource(R.color.login_input_stroke), shape)
}

private fun navigate(
    context: Context,
    selectedDestination: Destination,
    destination: Destination,
    activityClass: Class<out Activity>
) {
    if (selectedDestination == destination) {
        return
    }

    val activity = context.findActivity()
    activity.startActivity(Intent(activity, activityClass))
    activity.finish()
    activity.overridePendingTransition(0, 0)
}

private fun restartRoot(context: Context, activityClass: Class<out Activity>) {
    val activity = context.findActivity()
    val intent = Intent(activity, activityClass).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
    activity.startActivity(intent)
    activity.overridePendingTransition(0, 0)
}

private fun Context.findActivity(): Activity {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> error("Activity context required")
    }
}

private fun showDatePicker(context: Context, onSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onSelected(String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        setTitle(R.string.create_task_pick_date)
        show()
    }
}

private fun showTimePicker(context: Context, onSelected: (String) -> Unit) {
    val hoursPicker = NumberPicker(context).apply {
        minValue = 0
        maxValue = 24
        wrapSelectorWheel = false
    }
    val minutesPicker = NumberPicker(context).apply {
        minValue = 0
        maxValue = 3
        displayedValues = arrayOf("00", "15", "30", "45")
        wrapSelectorWheel = false
    }
    val pickerRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        val padding = (18 * resources.displayMetrics.density).toInt()
        setPadding(padding, (8 * resources.displayMetrics.density).toInt(), padding, 0)
        addView(hoursPicker, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        addView(minutesPicker, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
    }

    AlertDialog.Builder(context)
        .setTitle(R.string.create_task_pick_time)
        .setView(pickerRow)
        .setPositiveButton(android.R.string.ok) { _, _ ->
            val minutes = minutesPicker.value * 15
            onSelected(String.format(Locale.US, "%dh %02dm", hoursPicker.value, minutes))
        }
        .setNegativeButton(android.R.string.cancel, null)
        .show()
}

private fun showCompletionPicker(context: Context, onSelected: (String) -> Unit) {
    val picker = NumberPicker(context).apply {
        minValue = 0
        maxValue = 100
        wrapSelectorWheel = false
        value = 0
        setFormatter { "$it%" }
        val horizontalPadding = (18 * resources.displayMetrics.density).toInt()
        setPadding(horizontalPadding, (8 * resources.displayMetrics.density).toInt(), horizontalPadding, 0)
    }

    AlertDialog.Builder(context)
        .setTitle(R.string.create_task_pick_completion)
        .setView(picker)
        .setPositiveButton(android.R.string.ok) { _, _ ->
            onSelected(String.format(Locale.US, "%d%%", picker.value))
        }
        .setNegativeButton(android.R.string.cancel, null)
        .show()
}
