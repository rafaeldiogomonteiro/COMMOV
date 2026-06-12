package com.example.commov.ui.compose

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.layout.ContentScale
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
import androidx.core.content.FileProvider
import android.content.ContentValues
import java.io.File
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.commov.MainActivity
import com.example.commov.R
import com.example.commov.data.local.LanguageFlagStore
import com.example.commov.data.local.LocaleHelper
import com.example.commov.data.local.OnboardingStore
import com.example.commov.data.local.PendingProfilePhotoStore
import com.example.commov.data.local.ProfilePhotoImageReader
import com.example.commov.data.local.SessionManager
import com.example.commov.data.sync.ProfilePhotoSyncManager
import com.example.commov.data.remote.AuthApi
import com.example.commov.data.remote.AdminApi
import com.example.commov.data.remote.AdminMutationResult
import com.example.commov.data.remote.AdminUsersResult
import com.example.commov.data.remote.StatisticsApi
import com.example.commov.data.remote.StatisticsExportResult
import com.example.commov.data.remote.StatisticsUsersResult
import com.example.commov.data.remote.ProjectsResult
import com.example.commov.data.remote.ApiProject
import com.example.commov.data.remote.ApiTask
import com.example.commov.data.remote.ApiTaskTimeEntry
import com.example.commov.data.remote.ApiUser
import com.example.commov.data.remote.CreateUserInput
import com.example.commov.data.remote.CreateProjectInput
import com.example.commov.data.remote.CreateProjectResult
import com.example.commov.data.remote.CreateTaskInput as RemoteCreateTaskInput
import com.example.commov.data.remote.CreateTaskResult
import com.example.commov.data.remote.ProjectDetailResult
import com.example.commov.data.remote.ProjectMutationResult
import com.example.commov.data.remote.ProjectsApi
import com.example.commov.data.remote.PhotoApi
import com.example.commov.data.remote.PhotoUploadResult
import com.example.commov.data.remote.ProfileUpdateResult
import com.example.commov.data.remote.TaskApi
import com.example.commov.data.remote.UpdateProjectInput
import com.example.commov.data.remote.UserApi
import com.example.commov.data.remote.UpdateUserInput
import com.example.commov.data.remote.TaskMutationResult
import com.example.commov.data.remote.TaskTimeEntriesResult
import com.example.commov.data.remote.TaskResult
import com.example.commov.data.remote.UsersResult
import com.example.commov.model.DashboardTask
import com.example.commov.model.Project
import com.example.commov.model.ProjectMember
import com.example.commov.model.ProjectTask
import com.example.commov.ui.admin.AdminActivity
import com.example.commov.ui.dashboard.DashboardActivity
import com.example.commov.ui.intro.IntroActivity
import com.example.commov.ui.projects.CreateProjectActivity
import com.example.commov.ui.projects.CreateTaskActivity
import com.example.commov.ui.projects.ProjectsActivity
import com.example.commov.ui.projects.TaskDetailActivity
import com.example.commov.ui.projects.TasksActivity
import com.example.commov.ui.settings.SettingsActivity
import com.example.commov.ui.statistics.StatisticsActivity
import com.example.commov.viewmodel.DashboardPresentation
import com.example.commov.viewmodel.DashboardUiState
import com.example.commov.viewmodel.DashboardViewModel
import com.example.commov.viewmodel.LoginUiState
import com.example.commov.viewmodel.LoginViewModel
import com.example.commov.viewmodel.ProjectsUiState
import com.example.commov.viewmodel.ProjectsViewModel
import com.example.commov.viewmodel.SettingsViewModel
import com.example.commov.viewmodel.TasksUiState
import com.example.commov.viewmodel.TasksViewModel
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
    val pendingPhotoStore = remember { PendingProfilePhotoStore(context.applicationContext) }
    val onboardingStore = remember { OnboardingStore(context.applicationContext) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    var state by remember { mutableStateOf(LoginUiState("", "", false, 0, 0, 0, false, false, false)) }
    var isSavingPhoto by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    val offlinePhotoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null || isSavingPhoto) {
            return@rememberLauncherForActivityResult
        }

        isSavingPhoto = true
        val appContext = context.applicationContext
        runCatching {
            appContext.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        Thread {
            val fileName = uri.lastPathSegment?.substringAfterLast('/')?.ifBlank { "profile.jpg" } ?: "profile.jpg"
            val normalizedFileName = if (fileName.contains('.')) fileName else "$fileName.jpg"
            val bytes = ProfilePhotoImageReader.readCompressedJpeg(appContext, uri)
                ?: ProfilePhotoImageReader.readRawBytes(appContext, uri)
            val saveResult = runCatching {
                requireNotNull(bytes) { "Could not read selected image" }
                require(bytes.isNotEmpty()) { "Selected image is empty" }
                pendingPhotoStore.save(normalizedFileName, "image/jpeg", bytes)
            }

            mainHandler.post {
                isSavingPhoto = false
                if (saveResult.isSuccess) {
                    Toast.makeText(context, R.string.login_photo_saved_offline, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, R.string.login_photo_save_error, Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    DisposableEffect(viewModel) {
        viewModel.observe { state = it }
        onDispose { }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(state.loginAccepted) {
        if (state.loginAccepted) {
            Thread {
                ProfilePhotoSyncManager.syncIfNeeded(context.applicationContext)
                mainHandler.post {
                    activity.startActivity(Intent(activity, DashboardActivity::class.java))
                    activity.finish()
                }
            }.start()
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
        if (state.isCheckingSession) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.app_name),
                    color = colorResource(R.color.login_text_primary),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.login_restoring_session),
                    modifier = Modifier.padding(top = 12.dp),
                    color = colorResource(R.color.login_text_secondary),
                    fontSize = 14.sp
                )
            }
        } else {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
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
            OutlinedActionButton(
                text = stringResource(R.string.login_update_profile_picture),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(48.dp),
                enabled = !isSavingPhoto && !state.isLoading,
                onClick = { offlinePhotoPickerLauncher.launch(arrayOf("image/*")) }
            )
        }
        }

        if (!state.isCheckingSession) {
        Text(
            text = stringResource(R.string.login_reset_intro),
            color = colorResource(R.color.login_text_secondary).copy(alpha = 0.55f),
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .clickable {
                    Thread {
                        onboardingStore.resetIntro()
                        mainHandler.post {
                            activity.startActivity(Intent(activity, IntroActivity::class.java))
                            activity.finish()
                        }
                    }.start()
                }
        )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
                inProgressCount = 0,
                blockedCount = 0,
                tasks = emptyList(),
                overdueTasks = emptyList(),
                todayTasks = emptyList(),
                weekTasks = emptyList(),
                projects = emptyList(),
                hoursLoggedThisWeek = 0.0,
                tasksOverEstimate = 0,
                canManageProjects = sessionManager.canManageProjects(),
                requiresLogin = false,
                isLoading = true
            )
        )
    }
    val pullRefreshState = rememberPullToRefreshState()
    val profileMember = remember(state.userName) {
        sessionManager.currentUser()?.let { user ->
            ProjectMember(
                userId = user.userId,
                name = user.name,
                initials = user.name
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .joinToString("") { it.first().uppercase() }
                    .ifBlank { "U" },
                avatarColorResId = R.color.bottom_nav_selected,
                photo = user.photo
            )
        }
    }

    DisposableEffect(viewModel) {
        viewModel.observe { state = it }
        onDispose { }
    }

    LaunchedEffect(Unit) {
        Thread {
            ProfilePhotoSyncManager.syncIfNeeded(context.applicationContext)
        }.start()
    }

    LaunchedEffect(state.requiresLogin) {
        if (state.requiresLogin) {
            activity.startActivity(Intent(activity, MainActivity::class.java))
            activity.finish()
        }
    }

    AppScaffold(selectedDestination = Destination.HOME) {
        if (state.isLoading) {
            DashboardScreenSkeleton(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .screenContentPadding()
            )
        } else {
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { viewModel.reload() },
                state = pullRefreshState,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .screenContentPadding()
                ) {
                    DashboardHeader(
                        userName = state.userName.ifBlank {
                            sessionManager.currentUser()?.name?.trim().orEmpty()
                        }.ifBlank { "…" },
                        openTasksCount = state.pendingTasks,
                        profileMember = profileMember
                    )
                    if (state.canManageProjects) {
                        DashboardQuickActions(
                            onCreateTask = {
                                context.startActivity(Intent(context, ProjectsActivity::class.java))
                            },
                            onCreateProject = {
                                context.startActivity(Intent(context, CreateProjectActivity::class.java))
                            }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DashboardSummaryCard(
                            titleResId = R.string.dashboard_pending_tasks,
                            count = state.pendingTasks,
                            progress = state.pendingProgress,
                            iconResId = R.drawable.ic_clock,
                            progressColorResId = R.color.task_red,
                            iconBackgroundColorResId = R.color.task_red_soft,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardSummaryCard(
                            titleResId = R.string.dashboard_completed_tasks,
                            count = state.completedTasks,
                            progress = state.completedProgress,
                            iconResId = R.drawable.ic_check_circle,
                            progressColorResId = R.color.project_green,
                            iconBackgroundColorResId = R.color.project_green_soft,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DashboardMetricChip(
                            labelResId = R.string.dashboard_in_progress,
                            count = state.inProgressCount,
                            accentColorResId = R.color.task_orange,
                            backgroundColorResId = R.color.task_orange_soft,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardMetricChip(
                            labelResId = R.string.dashboard_blocked,
                            count = state.blockedCount,
                            accentColorResId = R.color.task_red,
                            backgroundColorResId = R.color.task_red_soft,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    DashboardTimeSummary(
                        hoursLoggedThisWeek = state.hoursLoggedThisWeek,
                        tasksOverEstimate = state.tasksOverEstimate,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    if (state.overdueTasks.isNotEmpty()) {
                        DashboardTaskSection(
                            titleResId = R.string.dashboard_overdue,
                            tasks = state.overdueTasks.take(3),
                            onSeeAll = {
                                context.startActivity(Intent(context, TasksActivity::class.java))
                            },
                            showSeeAll = false,
                            topPadding = 24.dp
                        )
                    }
                    if (state.todayTasks.isNotEmpty()) {
                        DashboardTaskSection(
                            titleResId = R.string.dashboard_for_today,
                            tasks = state.todayTasks.take(3),
                            onSeeAll = {
                                context.startActivity(Intent(context, TasksActivity::class.java))
                            },
                            showSeeAll = false,
                            topPadding = if (state.overdueTasks.isEmpty()) 24.dp else 16.dp
                        )
                    }
                    if (state.weekTasks.isNotEmpty()) {
                        DashboardTaskSection(
                            titleResId = R.string.dashboard_this_week,
                            tasks = state.weekTasks.take(3),
                            onSeeAll = {
                                context.startActivity(Intent(context, TasksActivity::class.java))
                            },
                            showSeeAll = false,
                            topPadding = 16.dp
                        )
                    }
                    DashboardProjectsSection(
                        projects = state.projects,
                        onProjectClick = { _ ->
                            context.startActivity(Intent(context, ProjectsActivity::class.java))
                        },
                        onSeeAll = {
                            context.startActivity(Intent(context, ProjectsActivity::class.java))
                        },
                        modifier = Modifier.padding(top = 24.dp)
                    )
                    DashboardTaskSection(
                        titleResId = R.string.dashboard_my_tasks,
                        tasks = state.tasks,
                        onSeeAll = {
                            context.startActivity(Intent(context, TasksActivity::class.java))
                        },
                        showSeeAll = true,
                        topPadding = 24.dp,
                        emptyContent = {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.dashboard_empty_tasks),
                                    color = colorResource(R.color.dashboard_text_secondary),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                                OutlinedActionButton(
                                    text = stringResource(R.string.dashboard_view_projects),
                                    modifier = Modifier
                                        .padding(top = 14.dp)
                                        .fillMaxWidth(0.7f)
                                        .height(44.dp),
                                    onClick = {
                                        context.startActivity(Intent(context, ProjectsActivity::class.java))
                                    }
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AllTasksScreen() {
    val context = LocalContext.current
    val activity = context.findActivity()
    val viewModel = remember { TasksViewModel(context.applicationContext) }
    var state by remember {
        mutableStateOf(TasksUiState(tasks = emptyList(), requiresLogin = false, isLoading = true))
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

    SubPageScaffold {
        SubPageContent {
            PageTopBar(
                title = stringResource(R.string.all_tasks_title),
                subtitle = stringResource(R.string.all_tasks_subtitle),
                onBack = { activity.finish() }
            )
            if (state.isLoading) {
                repeat(4) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .height(66.dp)
                            .cardBackground(R.color.dashboard_card, R.color.dashboard_card_stroke, 8.dp)
                    ) {}
                }
            } else if (state.tasks.isEmpty()) {
                Text(
                    text = stringResource(R.string.all_tasks_empty),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    color = colorResource(R.color.dashboard_text_secondary),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            } else {
                state.tasks.forEach { task ->
                    DashboardTaskCard(
                        task = task,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(66.dp)
                            .padding(bottom = 8.dp),
                        onClick = { openTaskDetail(context, task.taskId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(
    userName: String,
    openTasksCount: Int,
    profileMember: ProjectMember?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (profileMember != null) {
            Avatar(member = profileMember, size = 52.dp)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = if (profileMember != null) 14.dp else 0.dp)
        ) {
            Text(
                text = stringResource(R.string.dashboard_greeting, userName),
                color = colorResource(R.color.dashboard_text_primary),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.dashboard_today_date, DashboardPresentation.todayLabel()),
                modifier = Modifier.padding(top = 2.dp),
                color = colorResource(R.color.dashboard_text_secondary),
                fontSize = 13.sp
            )
            Text(
                text = stringResource(R.string.dashboard_open_tasks_subtitle, openTasksCount),
                modifier = Modifier.padding(top = 2.dp),
                color = colorResource(R.color.bottom_nav_selected),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun DashboardQuickActions(
    onCreateTask: () -> Unit,
    onCreateProject: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedActionButton(
            text = stringResource(R.string.dashboard_create_task),
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            onClick = onCreateTask
        )
        FilledActionButton(
            text = stringResource(R.string.dashboard_create_project),
            colorResId = R.color.login_button,
            radius = 6.dp,
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            onClick = onCreateProject
        )
    }
}

@Composable
private fun DashboardMetricChip(
    @StringRes labelResId: Int,
    count: Int,
    @ColorRes accentColorResId: Int,
    @ColorRes backgroundColorResId: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(44.dp)
            .cardBackground(R.color.dashboard_card, R.color.dashboard_card_stroke, 8.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(colorResource(accentColorResId))
        )
        Text(
            text = stringResource(labelResId),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            color = colorResource(R.color.dashboard_text_secondary),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = count.toString(),
            color = colorResource(accentColorResId),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DashboardTimeSummary(
    hoursLoggedThisWeek: Double,
    tasksOverEstimate: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .cardBackground(R.color.dashboard_card, R.color.dashboard_card_stroke, 10.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBubble(
                backgroundColorResId = R.color.task_blue_soft,
                iconResId = R.drawable.ic_clock,
                iconTintColorResId = R.color.bottom_nav_selected,
                size = 32.dp,
                iconSize = 16.dp,
                radius = 16.dp
            )
            Text(
                text = stringResource(
                    R.string.dashboard_hours_this_week,
                    formatDashboardHours(hoursLoggedThisWeek)
                ),
                modifier = Modifier.padding(start = 10.dp),
                color = colorResource(R.color.dashboard_text_primary),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
        if (tasksOverEstimate > 0) {
            Text(
                text = stringResource(R.string.dashboard_over_estimate, tasksOverEstimate),
                color = colorResource(R.color.task_orange),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun DashboardProjectsSection(
    projects: List<Project>,
    onProjectClick: (Int) -> Unit,
    onSeeAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        DashboardSectionHeader(
            titleResId = R.string.dashboard_your_projects,
            actionLabelResId = R.string.dashboard_see_all_projects,
            onAction = onSeeAll
        )
        if (projects.isEmpty()) {
            Text(
                text = stringResource(R.string.projects_empty),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                color = colorResource(R.color.dashboard_text_secondary),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        } else {
            projects.forEach { project ->
                ProjectCard(
                    project = project,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    onClick = { onProjectClick(project.projectId) }
                )
            }
        }
    }
}

@Composable
private fun DashboardTaskSection(
    @StringRes titleResId: Int,
    tasks: List<DashboardTask>,
    onSeeAll: () -> Unit,
    showSeeAll: Boolean,
    topPadding: Dp,
    emptyContent: (@Composable () -> Unit)? = null
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding)
    ) {
        DashboardSectionHeader(
            titleResId = titleResId,
            actionLabelResId = if (showSeeAll) R.string.dashboard_see_all else null,
            onAction = if (showSeeAll) onSeeAll else null
        )
        if (tasks.isEmpty()) {
            emptyContent?.invoke()
        } else {
            tasks.forEach { task ->
                DashboardTaskCard(
                    task = task,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(66.dp)
                        .padding(bottom = 8.dp),
                    onClick = { openTaskDetail(context, task.taskId) }
                )
            }
        }
    }
}

@Composable
private fun DashboardSectionHeader(
    @StringRes titleResId: Int,
    @StringRes actionLabelResId: Int?,
    onAction: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(titleResId),
            modifier = Modifier.weight(1f),
            color = colorResource(R.color.dashboard_text_primary),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        if (actionLabelResId != null && onAction != null) {
            Text(
                text = stringResource(actionLabelResId),
                modifier = Modifier
                    .clickable(onClick = onAction)
                    .padding(vertical = 8.dp),
                color = colorResource(R.color.bottom_nav_selected),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun openTaskDetail(context: Context, taskId: Int) {
    if (taskId <= 0) {
        return
    }
    val intent = Intent(context, TaskDetailActivity::class.java)
    intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId)
    context.startActivity(intent)
}

private fun formatDashboardHours(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(Locale.getDefault(), "%.1f", value)
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
                requiresLogin = false,
                isLoading = true
            )
        )
    }
    var selectedProjectId by remember { mutableStateOf<Int?>(null) }
    var showEditProject by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editDescription by remember { mutableStateOf("") }
    var editStatus by remember { mutableStateOf("active") }
    var showAddMember by remember { mutableStateOf(false) }
    var showDeleteProjectConfirm by remember { mutableStateOf(false) }
    var pickerUsers by remember { mutableStateOf<List<ApiUser>>(emptyList()) }
    val project = state.projects.firstOrNull { it.projectId == selectedProjectId }
    val projectDetailRefreshLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.reload()
        }
    }

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
                        val message = (result as? ProjectMutationResult.ServerError)?.message
                            ?.takeIf { it.isNotBlank() }
                            ?: context.getString(R.string.project_action_error)
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
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
        if (state.isLoading) {
            ProjectsScreenSkeleton(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .screenContentPadding()
            )
        } else {
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                }
            } else {
                ProjectDetailView(
                        project = project,
                        canManage = canManage,
                        canCreateTasks = state.canCreateTasks,
                        onBack = { selectedProjectId = null },
                        onEdit = {
                            editName = project.nameText.orEmpty()
                            editDescription = project.descriptionText.orEmpty()
                            editStatus = when (project.status.lowercase(Locale.getDefault())) {
                                "on_hold" -> "cancelled"
                                else -> project.status
                            }
                            showEditProject = true
                        },
                        onDelete = { showDeleteProjectConfirm = true },
                        onCreateTask = {
                            val intent = Intent(context, CreateTaskActivity::class.java)
                            intent.putExtra(CreateTaskActivity.EXTRA_PROJECT_ID, project.projectId)
                            intent.putExtra(
                                CreateTaskActivity.EXTRA_PROJECT_NAME,
                                project.nameText ?: context.getString(project.nameResId)
                            )
                            projectDetailRefreshLauncher.launch(intent)
                        },
                        onTaskClick = { taskId ->
                            val intent = Intent(context, TaskDetailActivity::class.java)
                            intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId)
                            projectDetailRefreshLauncher.launch(intent)
                        },
                        onRemoveMember = { member ->
                            runProjectMutation(
                                mutation = { token ->
                                    projectsApi.removeMember(token, project.projectId, member.userId)
                                },
                                successMessageResId = R.string.project_member_removed
                            )
                        },
                        onAddMember = {
                            val token = sessionManager.token() ?: return@ProjectDetailView
                            Thread {
                                when (val result = projectsApi.users(token)) {
                                    is UsersResult.Success -> {
                                        val existingIds = project.members.map { it.userId }.toSet()
                                        mainHandler.post {
                                            pickerUsers = result.users.filter {
                                                it.active && it.userId !in existingIds
                                            }
                                            showAddMember = true
                                        }
                                    }
                                    UsersResult.Unauthorized -> mainHandler.post {
                                        sessionManager.clear()
                                        activity.startActivity(Intent(activity, MainActivity::class.java))
                                        activity.finish()
                                    }
                                    UsersResult.NetworkError,
                                    is UsersResult.ServerError -> mainHandler.post {
                                        Toast.makeText(context, R.string.project_action_error, Toast.LENGTH_LONG).show()
                                    }
                                }
                            }.start()
                        }
                    )
            }
        }
        }
    }

    if (showDeleteProjectConfirm && project != null) {
        ConfirmActionDialog(
            title = stringResource(R.string.confirm_delete_title),
            message = stringResource(R.string.confirm_delete_project_message),
            confirmText = stringResource(R.string.project_delete),
            onDismiss = { showDeleteProjectConfirm = false },
            onConfirm = {
                showDeleteProjectConfirm = false
                val deletedProjectId = project.projectId
                runProjectMutation(
                    mutation = { token -> projectsApi.deleteProject(token, deletedProjectId) },
                    successMessageResId = R.string.project_deleted,
                    onSuccess = {
                        selectedProjectId = null
                        viewModel.removeProject(deletedProjectId)
                    }
                )
            }
        )
    }

    if (showAddMember && project != null) {
        UserPickerDialog(
            title = stringResource(R.string.project_add_member),
            users = pickerUsers,
            emptyMessage = stringResource(R.string.project_no_users_available),
            onDismiss = { showAddMember = false },
            onUserSelected = { user ->
                showAddMember = false
                runProjectMutation(
                    mutation = { token -> projectsApi.addMember(token, project.projectId, user.userId) },
                    successMessageResId = R.string.project_member_added
                )
            }
        )
    }

    if (showEditProject && project != null) {
        EditProjectDialog(
            name = editName,
            onNameChange = { editName = it },
            description = editDescription,
            onDescriptionChange = { editDescription = it },
            status = editStatus,
            statusOptions = projectStatusLabels(),
            statusValues = projectStatusValues(),
            onStatusChange = { editStatus = it },
            onDismiss = { showEditProject = false },
            onSave = {
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
        )
    }
}

@Composable
private fun SubPageScaffold(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.dashboard_background))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        content()
    }
}

@Composable
private fun SubPageContent(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .screenContentPadding(bottom = 28.dp),
        content = content
    )
}

@Composable
private fun PageTopBar(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(colorResource(R.color.dashboard_card))
                .border(1.dp, colorResource(R.color.dashboard_card_stroke), CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = stringResource(R.string.action_back),
                modifier = Modifier
                    .size(18.dp)
                    .rotate(180f),
                colorFilter = ColorFilter.tint(colorResource(R.color.dashboard_text_primary))
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        ) {
            Text(
                text = title,
                color = colorResource(R.color.dashboard_text_primary),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    modifier = Modifier.padding(top = 3.dp),
                    color = colorResource(R.color.dashboard_text_secondary),
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ProjectDetailView(
    project: Project,
    canManage: Boolean,
    canCreateTasks: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCreateTask: () -> Unit,
    onTaskClick: (Int) -> Unit,
    onRemoveMember: (ProjectMember) -> Unit,
    onAddMember: () -> Unit
) {
    val projectName = project.nameText ?: stringResource(project.nameResId)
    val projectDescription = project.descriptionText ?: stringResource(project.descriptionResId)

    Column(modifier = Modifier.fillMaxWidth()) {
        PageTopBar(
            title = projectName,
            subtitle = stringResource(R.string.project_detail_back),
            onBack = onBack
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(colorResource(project.accentColorResId))
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(26.dp))
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = project.initials,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 14.dp)
                    ) {
                        StatusPill(
                            status = project.status,
                            onDark = true,
                            labelProvider = { projectStatusLabel(it) }
                        )
                        Text(
                            text = stringResource(R.string.project_tasks_count, project.taskCount),
                            modifier = Modifier.padding(top = 8.dp),
                            color = Color.White.copy(alpha = 0.88f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                if (projectDescription.isNotBlank()) {
                    Text(
                        text = projectDescription,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        color = Color.White.copy(alpha = 0.92f),
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.project_detail_overview).uppercase(Locale.getDefault()),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.dp, bottom = 10.dp),
            color = colorResource(R.color.dashboard_text_secondary),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DetailStatTile(
                label = stringResource(R.string.create_project_start_date),
                value = project.startDate?.take(10).orEmpty().ifBlank { "—" },
                modifier = Modifier.weight(1f)
            )
            DetailStatTile(
                label = stringResource(R.string.create_project_estimated_end_date),
                value = project.estimatedEndDate?.take(10).orEmpty().ifBlank { "—" },
                modifier = Modifier.weight(1f)
            )
            DetailStatTile(
                label = stringResource(R.string.create_project_manager),
                value = project.managerName.orEmpty().ifBlank { "—" },
                modifier = Modifier.weight(1f)
            )
        }

        if (canCreateTasks) {
            FilledActionButton(
                text = stringResource(R.string.project_create_task),
                colorResId = R.color.login_button,
                radius = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
                    .height(50.dp),
                onClick = onCreateTask
            )
        }

        DetailSectionHeader(
            title = stringResource(R.string.project_detail_tasks),
            count = project.tasks.size,
            topPadding = 24.dp
        )
        if (project.tasks.isEmpty()) {
            EmptyStateCard(text = stringResource(R.string.project_detail_no_tasks))
        } else {
            project.tasks.forEach { projectTask ->
                val taskId = projectTask.task.taskId
                ProjectTaskListItem(
                    projectTask = projectTask,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    onClick = if (taskId > 0) {
                        { onTaskClick(taskId) }
                    } else {
                        null
                    }
                )
            }
        }

        DetailSectionHeader(
            title = stringResource(R.string.project_detail_people),
            count = project.members.size,
            topPadding = 20.dp,
            onAddClick = if (canManage) onAddMember else null
        )
        project.members.chunked(2).forEach { rowMembers ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowMembers.forEach { member ->
                    TeamMemberCard(
                        member = member,
                        modifier = Modifier.weight(1f),
                        onRemove = if (canManage && !member.isManager && member.userId > 0) {
                            { onRemoveMember(member) }
                        } else {
                            null
                        }
                    )
                }
                if (rowMembers.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        if (canManage) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlineActionButton(
                    text = stringResource(R.string.project_edit),
                    colorResId = R.color.bottom_nav_selected,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    onClick = onEdit
                )
                OutlineActionButton(
                    text = stringResource(R.string.project_delete),
                    colorResId = R.color.settings_logout,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    onClick = onDelete
                )
            }
        }
    }
}

@Composable
private fun DetailSectionHeader(
    title: String,
    count: Int,
    topPadding: Dp,
    onAddClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = colorResource(R.color.dashboard_text_primary),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        if (onAddClick != null) {
            AddCircleButton(
                modifier = Modifier.padding(end = 8.dp),
                onClick = onAddClick
            )
        }
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(colorResource(R.color.login_button))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = count.toString(),
                color = colorResource(R.color.white),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AddCircleButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(colorResource(R.color.login_button))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            color = colorResource(R.color.white),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DetailStatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colorResource(R.color.dashboard_card))
            .border(1.dp, colorResource(R.color.dashboard_card_stroke), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 12.dp)
    ) {
        Text(
            text = label.uppercase(Locale.getDefault()),
            color = colorResource(R.color.dashboard_text_secondary),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            modifier = Modifier.padding(top = 6.dp),
            color = colorResource(R.color.dashboard_text_primary),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EmptyStateCard(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorResource(R.color.dashboard_muted))
            .padding(horizontal = 16.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = colorResource(R.color.dashboard_text_secondary),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProjectTaskListItem(
    projectTask: ProjectTask,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val task = projectTask.task
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colorResource(R.color.dashboard_card))
            .border(1.dp, colorResource(R.color.dashboard_card_stroke), RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colorResource(task.accentColorResId))
        )
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
                modifier = Modifier.padding(top = 3.dp),
                color = colorResource(R.color.dashboard_text_secondary),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        StatusPill(status = task.statusText ?: "pending")
        if (projectTask.assignees.isNotEmpty()) {
            AvatarStack(
                members = projectTask.assignees,
                avatarSize = 28.dp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        CardIcon(
            iconResId = R.drawable.ic_arrow_right,
            containerSize = 28.dp,
            iconSize = 14.dp,
            cornerRadius = 8.dp,
            backgroundColorResId = R.color.dashboard_muted,
            tintColorResId = R.color.dashboard_text_secondary,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun TeamMemberCard(
    member: ProjectMember,
    modifier: Modifier = Modifier,
    onRemove: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colorResource(R.color.dashboard_card))
            .border(1.dp, colorResource(R.color.dashboard_card_stroke), RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Avatar(member = member, size = 44.dp)
        Text(
            text = member.name,
            modifier = Modifier.padding(top = 10.dp),
            color = colorResource(R.color.dashboard_text_primary),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        if (member.isManager) {
            Text(
                text = stringResource(R.string.create_project_manager),
                modifier = Modifier.padding(top = 4.dp),
                color = colorResource(R.color.bottom_nav_selected),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        if (onRemove != null) {
            Text(
                text = stringResource(R.string.project_remove_member),
                modifier = Modifier
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onRemove)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                color = colorResource(R.color.settings_logout),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatusPill(
    status: String,
    modifier: Modifier = Modifier,
    onDark: Boolean = false,
    labelProvider: @Composable (String) -> String = { taskStatusLabel(it) }
) {
    val normalized = status.lowercase(Locale.getDefault())
    val backgroundColor = if (onDark) {
        Color.White.copy(alpha = 0.24f)
    } else {
        colorResource(
            when (normalized) {
                "completed", "done" -> R.color.project_green_soft
                "blocked", "cancelled", "on_hold" -> R.color.task_red_soft
                "in_progress", "active", "pending" -> R.color.task_blue_soft
                else -> R.color.task_status_gray_bg
            }
        )
    }
    val textColor = if (onDark) {
        Color.White
    } else {
        colorResource(
            when (normalized) {
                "completed", "done" -> R.color.project_green
                "blocked", "cancelled", "on_hold" -> R.color.task_red
                "in_progress", "active" -> R.color.bottom_nav_selected
                else -> R.color.task_status_gray_text
            }
        )
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = labelProvider(status).uppercase(Locale.getDefault()),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun OutlineActionButton(
    text: String,
    @ColorRes colorResId: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, colorResource(colorResId), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = colorResource(colorResId),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TaskInfoTile(
    label: String,
    value: String,
    @DrawableRes iconResId: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 112.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colorResource(R.color.dashboard_card))
            .border(1.dp, colorResource(R.color.dashboard_card_stroke), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CardIcon(
            iconResId = iconResId,
            containerSize = 38.dp,
            iconSize = 20.dp
        )
        Text(
            text = label.uppercase(Locale.getDefault()),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            color = colorResource(R.color.dashboard_text_secondary),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 11.sp
        )
        Text(
            text = value.ifBlank { "—" },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            color = colorResource(R.color.dashboard_text_primary),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun CardIcon(
    @DrawableRes iconResId: Int,
    modifier: Modifier = Modifier,
    containerSize: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    cornerRadius: Dp = 12.dp,
    @ColorRes backgroundColorResId: Int = R.color.task_blue_soft,
    @ColorRes tintColorResId: Int = R.color.bottom_nav_selected
) {
    Box(
        modifier = modifier
            .size(containerSize)
            .clip(RoundedCornerShape(cornerRadius))
            .background(colorResource(backgroundColorResId)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconResId),
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(colorResource(tintColorResId))
        )
    }
}

@Composable
private fun TaskProgressCard(estimatedTime: Double, timeSpent: Double) {
    val progress = if (estimatedTime > 0) {
        (timeSpent / estimatedTime).coerceIn(0.0, 1.0).toFloat()
    } else {
        0f
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.dashboard_card))
            .border(1.dp, colorResource(R.color.dashboard_card_stroke), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.task_detail_progress),
                modifier = Modifier.weight(1f),
                color = colorResource(R.color.dashboard_text_primary),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(
                    R.string.task_detail_hours,
                    formatHours(timeSpent),
                    formatHours(estimatedTime)
                ),
                color = colorResource(R.color.bottom_nav_selected),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(colorResource(R.color.dashboard_muted))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(colorResource(R.color.login_button))
            )
        }
    }
}

private fun formatHours(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}

private fun isDateAfter(date: String?, reference: String?): Boolean {
    val normalizedDate = date?.take(10)?.takeIf { it.length == 10 } ?: return false
    val normalizedReference = reference?.take(10)?.takeIf { it.length == 10 } ?: return false
    return normalizedDate > normalizedReference
}

private fun projectStatusValues(): List<String> = listOf("active", "completed", "cancelled")

@Composable
private fun projectStatusLabels(): List<String> {
    return projectStatusValues().map { projectStatusLabel(it) }
}

@Composable
private fun projectStatusLabel(status: String): String {
    return when (status.lowercase(Locale.getDefault())) {
        "completed" -> stringResource(R.string.project_status_completed)
        "cancelled" -> stringResource(R.string.project_status_cancelled)
        "on_hold" -> stringResource(R.string.project_status_on_hold)
        else -> stringResource(R.string.project_status_active)
    }
}

private fun ApiUser.toProjectMember(index: Int, isManager: Boolean): ProjectMember {
    return ProjectMember(
        userId = userId,
        name = name,
        initials = name
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "U" },
        avatarColorResId = when (index % 5) {
            0 -> R.color.bottom_nav_selected
            1 -> R.color.project_green
            2 -> R.color.task_orange
            3 -> R.color.project_purple
            else -> R.color.task_status_gray_text
        },
        isManager = isManager,
        photo = photo
    )
}

@Composable
private fun TimeSpentSummaryCard(timeSpent: Double, estimatedTime: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorResource(R.color.dashboard_card))
            .border(1.dp, colorResource(R.color.dashboard_card_stroke), RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Text(
            text = stringResource(R.string.task_detail_time_spent),
            color = colorResource(R.color.dashboard_text_secondary),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.task_detail_time_spent_value, formatHours(timeSpent)),
            modifier = Modifier.padding(top = 8.dp),
            color = colorResource(R.color.dashboard_text_primary),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        if (estimatedTime > 0) {
            Text(
                text = stringResource(R.string.task_detail_time_spent_of, formatHours(estimatedTime)),
                modifier = Modifier.padding(top = 4.dp),
                color = colorResource(R.color.dashboard_text_secondary),
                fontSize = 14.sp
            )
            val progress = (timeSpent / estimatedTime).coerceIn(0.0, 1.0).toFloat()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colorResource(R.color.dashboard_muted))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(colorResource(R.color.login_button))
                )
            }
        }
    }
}

@Composable
private fun TimeSpentEntryCard(entry: ApiTaskTimeEntry, modifier: Modifier = Modifier) {
    val member = ProjectMember(
        userId = entry.userId,
        name = entry.userName,
        initials = entry.userName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "U" },
        avatarColorResId = R.color.bottom_nav_selected,
        photo = entry.userPhoto
    )
    val workDateLabel = entry.workDate?.take(10).orEmpty()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorResource(R.color.dashboard_card))
            .border(1.dp, colorResource(R.color.dashboard_card_stroke), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Avatar(member = member, size = 36.dp)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.userName,
                    modifier = Modifier.weight(1f),
                    color = colorResource(R.color.dashboard_text_primary),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.task_detail_time_entry_hours, formatHours(entry.timeSpent)),
                    color = colorResource(R.color.login_button),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (workDateLabel.isNotBlank()) {
                Text(
                    text = workDateLabel,
                    modifier = Modifier.padding(top = 2.dp),
                    color = colorResource(R.color.dashboard_text_secondary),
                    fontSize = 11.sp
                )
            }
            if (entry.observation.isNotBlank()) {
                Text(
                    text = entry.observation,
                    modifier = Modifier.padding(top = 6.dp),
                    color = colorResource(R.color.dashboard_text_primary),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
            if (entry.photo.isNotBlank()) {
                val photoUrl = remember(entry.photo) { PhotoApi().photoUrl(entry.photo) }
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.task_detail_photo),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .heightIn(max = 220.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.FillWidth
                )
            }
        }
    }
}

@Composable
private fun AddTimeSpentDialog(
    onDismiss: () -> Unit,
    onConfirm: (hours: Double, observation: String, imageUri: Uri?) -> Unit
) {
    val context = LocalContext.current
    var hours by remember { mutableStateOf("") }
    var observation by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var requiredError by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
    }

    AppFormDialog(
        title = stringResource(R.string.task_detail_add_time_spent),
        subtitle = stringResource(R.string.task_detail_work_date) + ": " + currentDateString(),
        onDismiss = onDismiss,
        confirmText = stringResource(R.string.action_save),
        onConfirm = {
            val parsedHours = parseDecimalInput(hours)
            if (parsedHours == null || parsedHours <= 0 || observation.trim().isEmpty()) {
                requiredError = true
            } else {
                onConfirm(parsedHours, observation.trim(), selectedImageUri)
            }
        }
    ) {
        CreateTaskLabelNoTop(R.string.task_detail_hours_to_add)
        CreateTaskInput(
            value = hours,
            onValueChange = {
                hours = it
                requiredError = false
            },
            singleLine = true,
            keyboardType = KeyboardType.Decimal
        )
        CreateTaskLabel(R.string.task_detail_observation)
        CreateTaskInput(
            value = observation,
            onValueChange = { observation = it },
            singleLine = false
        )
        CreateTaskLabel(R.string.task_detail_photo)
        if (selectedImageUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(selectedImageUri)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.task_detail_photo),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .heightIn(max = 180.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.FillWidth
            )
            Text(
                text = stringResource(R.string.task_detail_remove_photo),
                color = colorResource(R.color.login_link),
                fontSize = 13.sp,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { selectedImageUri = null }
            )
        } else {
            OutlinedActionButton(
                text = stringResource(R.string.task_detail_upload_photo),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .height(44.dp),
                onClick = { photoPickerLauncher.launch("image/*") }
            )
        }
        if (requiredError) {
            Text(
                text = stringResource(R.string.create_task_required_error),
                modifier = Modifier.padding(top = 8.dp),
                color = colorResource(R.color.login_error),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun UserPickerDialog(
    title: String,
    users: List<ApiUser>,
    emptyMessage: String,
    onDismiss: () -> Unit,
    onUserSelected: (ApiUser) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(colorResource(R.color.dashboard_card))
                .border(1.dp, colorResource(R.color.dashboard_card_stroke), RoundedCornerShape(18.dp))
                .padding(20.dp)
        ) {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                color = colorResource(R.color.dashboard_text_primary),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.project_select_member),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                color = colorResource(R.color.dashboard_text_secondary),
                fontSize = 14.sp,
                lineHeight = 19.sp
            )

            if (users.isEmpty()) {
                Box(Modifier.padding(top = 18.dp)) {
                    EmptyStateCard(text = emptyMessage)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .padding(top = 18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    users.forEach { user ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(colorResource(R.color.dashboard_background))
                                .border(1.dp, colorResource(R.color.dashboard_card_stroke), RoundedCornerShape(12.dp))
                                .clickable { onUserSelected(user) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Avatar(
                                member = user.toProjectMember(0, false),
                                size = 40.dp
                            )
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 12.dp)
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
                            }
                            Image(
                                painter = painterResource(R.drawable.ic_arrow_right),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(16.dp)
                                    .rotate(180f),
                                colorFilter = ColorFilter.tint(colorResource(R.color.dashboard_text_secondary))
                            )
                        }
                    }
                }
            }

            OutlineActionButton(
                text = stringResource(android.R.string.cancel),
                colorResId = R.color.dashboard_text_secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
                    .height(46.dp),
                onClick = onDismiss
            )
        }
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

    SubPageScaffold {
        SubPageContent {
            PageTopBar(
                title = stringResource(R.string.create_project_title),
                subtitle = stringResource(R.string.create_project_subtitle),
                onBack = { activity.finish() }
            )
            if (!usersLoaded) {
                CreateFormScreenSkeleton()
            } else {
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
    var assigneeIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var estimatedEndDate by remember { mutableStateOf("") }
    var estimatedTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var requiredError by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var projectEstimatedEndDate by remember { mutableStateOf<String?>(null) }
    val taskDateAfterProjectEnd = remember(estimatedEndDate, projectEstimatedEndDate) {
        isDateAfter(estimatedEndDate, projectEstimatedEndDate)
    }

    LaunchedEffect(projectId) {
        val token = sessionManager.token()
        if (token.isNullOrBlank() || projectId <= 0) {
            activity.startActivity(Intent(activity, MainActivity::class.java))
            activity.finish()
            return@LaunchedEffect
        }

        Thread {
            val projectResult = projectsApi.getProject(token, projectId)
            val result = projectsApi.projectUsers(token, projectId)
            mainHandler.post {
                when (projectResult) {
                    is ProjectDetailResult.Success -> {
                        projectEstimatedEndDate = projectResult.project.estimatedEndDate?.take(10)
                    }
                    ProjectDetailResult.Unauthorized -> {
                        sessionManager.clear()
                        activity.startActivity(Intent(activity, MainActivity::class.java))
                        activity.finish()
                        return@post
                    }
                    ProjectDetailResult.NetworkError,
                    is ProjectDetailResult.ServerError -> Unit
                }
                when (result) {
                    is UsersResult.Success -> {
                        assignees = result.users
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

    SubPageScaffold {
        SubPageContent {
            PageTopBar(
                title = stringResource(R.string.create_task_title),
                subtitle = stringResource(R.string.create_task_subtitle, resolvedProjectName),
                onBack = { activity.finish() }
            )
            if (!assigneesLoaded) {
                CreateFormScreenSkeleton()
            } else {
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
                    assignees.forEach { user ->
                        val selected = assigneeIds.contains(user.userId)
                        SelectableMemberRow(
                            user = user,
                            selected = selected,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .padding(top = 8.dp),
                            onClick = {
                                assigneeIds = if (selected) {
                                    assigneeIds - user.userId
                                } else {
                                    assigneeIds + user.userId
                                }
                                requiredError = false
                            }
                        )
                    }
                }
                CreateTaskLabel(R.string.create_task_description)
                CreateTaskInput(
                    value = description,
                    onValueChange = { description = it },
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
                if (taskDateAfterProjectEnd && !projectEstimatedEndDate.isNullOrBlank()) {
                    Text(
                        text = stringResource(
                            R.string.create_task_after_project_end_warning,
                            projectEstimatedEndDate.orEmpty()
                        ),
                        modifier = Modifier.padding(top = 8.dp),
                        color = colorResource(R.color.task_orange),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
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
                    val estimatedTimeValue = estimatedTime.toDoubleOrNull()
                    if (
                        projectId <= 0 ||
                        title.trim().isEmpty() ||
                        assigneeIds.isEmpty() ||
                        estimatedEndDate.isBlank() ||
                        estimatedTimeValue == null ||
                        estimatedTimeValue < 0
                    ) {
                        requiredError = true
                        return@FilledActionButton
                    }
                    if (taskDateAfterProjectEnd && !projectEstimatedEndDate.isNullOrBlank()) {
                        Toast.makeText(
                            context,
                            context.getString(
                                R.string.create_task_after_project_end_warning,
                                projectEstimatedEndDate
                            ),
                            Toast.LENGTH_LONG
                        ).show()
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
                                userIds = assigneeIds.toList(),
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
                                    activity.setResult(Activity.RESULT_OK)
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
}

@Composable
fun TaskDetailScreen(taskId: Int) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val taskApi = remember { TaskApi() }
    val photoApi = remember { PhotoApi() }
    val projectsApi = remember { ProjectsApi() }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val canManageTasks = remember { sessionManager.canManageProjects() }
    var task by remember { mutableStateOf<ApiTask?>(null) }
    var timeEntries by remember { mutableStateOf<List<ApiTaskTimeEntry>>(emptyList()) }
    var projectMembers by remember { mutableStateOf<List<ApiUser>>(emptyList()) }
    var projectManagerId by remember { mutableStateOf(0) }
    var showAddTimeDialog by remember { mutableStateOf(false) }
    var showAssigneePicker by remember { mutableStateOf(false) }
    var showDeleteTaskConfirm by remember { mutableStateOf(false) }
    var showCompleteTaskConfirm by remember { mutableStateOf(false) }
    var assigneePickerUsers by remember { mutableStateOf<List<ApiUser>>(emptyList()) }
    var isSaving by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    fun authFailure() {
        sessionManager.clear()
        activity.startActivity(Intent(activity, MainActivity::class.java))
        activity.finish()
    }

    fun loadTimeEntries() {
        val token = sessionManager.token()
        if (token.isNullOrBlank() || taskId <= 0) {
            return
        }
        Thread {
            when (val result = taskApi.listTimeEntries(token, taskId)) {
                is TaskTimeEntriesResult.Success -> {
                    mainHandler.post { timeEntries = result.entries }
                }
                TaskTimeEntriesResult.Unauthorized -> mainHandler.post { authFailure() }
                TaskTimeEntriesResult.NetworkError,
                is TaskTimeEntriesResult.ServerError -> Unit
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
                        isLoading = false
                        loadTimeEntries()
                        Thread {
                            val membersResult = projectsApi.projectUsers(token, result.task.projectId)
                            val managerId = when (val projectResult = projectsApi.getProject(token, result.task.projectId)) {
                                is ProjectDetailResult.Success -> projectResult.project.managerId
                                else -> 0
                            }
                            mainHandler.post {
                                projectManagerId = managerId
                                if (membersResult is UsersResult.Success) {
                                    projectMembers = membersResult.users
                                }
                            }
                        }.start()
                    }
                    TaskResult.Unauthorized -> authFailure()
                    TaskResult.NetworkError,
                    is TaskResult.ServerError -> {
                        isLoading = false
                        Toast.makeText(context, R.string.task_detail_error, Toast.LENGTH_LONG).show()
                    }
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
                    TaskMutationResult.NetworkError -> {
                        Toast.makeText(context, R.string.task_detail_error, Toast.LENGTH_LONG).show()
                    }
                    is TaskMutationResult.ServerError -> {
                        val message = result.message?.takeIf { it.isNotBlank() }
                            ?: context.getString(R.string.task_detail_error)
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }.start()
    }

    LaunchedEffect(taskId) {
        loadTask()
    }

    if (showCompleteTaskConfirm) {
        ConfirmActionDialog(
            title = stringResource(R.string.confirm_complete_title),
            message = stringResource(R.string.confirm_complete_task_message),
            confirmText = stringResource(R.string.task_detail_complete),
            confirmColorResId = R.color.project_green,
            onDismiss = { showCompleteTaskConfirm = false },
            onConfirm = {
                showCompleteTaskConfirm = false
                if (!isSaving) {
                    mutate(
                        action = { token ->
                            taskApi.complete(
                                token,
                                taskId,
                                currentDateString(),
                                task?.observation.orEmpty(),
                                photo = task?.photo,
                                location = task?.location
                            )
                        },
                        successMessage = R.string.task_detail_updated,
                        afterSuccess = {
                            activity.setResult(Activity.RESULT_OK)
                            loadTask()
                        }
                    )
                }
            }
        )
    }

    if (showDeleteTaskConfirm) {
        ConfirmActionDialog(
            title = stringResource(R.string.confirm_delete_title),
            message = stringResource(R.string.confirm_delete_task_message),
            confirmText = stringResource(R.string.task_detail_delete),
            onDismiss = { showDeleteTaskConfirm = false },
            onConfirm = {
                showDeleteTaskConfirm = false
                if (!isSaving) {
                    mutate(
                        action = { token -> taskApi.delete(token, taskId) },
                        successMessage = R.string.task_detail_deleted,
                        afterSuccess = {
                            activity.setResult(Activity.RESULT_OK)
                            activity.finish()
                        }
                    )
                }
            }
        )
    }

    if (showAddTimeDialog) {
        AddTimeSpentDialog(
            onDismiss = { showAddTimeDialog = false },
            onConfirm = { hours, observation, imageUri ->
                showAddTimeDialog = false
                val token = sessionManager.token()
                if (token.isNullOrBlank()) {
                    authFailure()
                } else {
                isSaving = true
                Thread {
                    var photoPath: String? = null
                    if (imageUri != null) {
                        val mimeType = context.contentResolver.getType(imageUri).orEmpty().ifBlank { "image/jpeg" }
                        val fileName = imageUri.lastPathSegment?.substringAfterLast('/')?.ifBlank { "timelog.jpg" } ?: "timelog.jpg"
                        val bytes = ProfilePhotoImageReader.readCompressedJpeg(context, imageUri)
                            ?: ProfilePhotoImageReader.readRawBytes(context, imageUri)
                        if (bytes == null || bytes.isEmpty()) {
                            mainHandler.post {
                                isSaving = false
                                Toast.makeText(context, R.string.settings_photo_error, Toast.LENGTH_LONG).show()
                            }
                            return@Thread
                        }
                        when (val uploadResult = photoApi.uploadPhoto(token, fileName, bytes, mimeType)) {
                            is PhotoUploadResult.Success -> photoPath = uploadResult.path
                            PhotoUploadResult.Unauthorized -> {
                                mainHandler.post {
                                    isSaving = false
                                    authFailure()
                                }
                                return@Thread
                            }
                            PhotoUploadResult.NetworkError,
                            is PhotoUploadResult.ServerError -> {
                                mainHandler.post {
                                    isSaving = false
                                    Toast.makeText(context, R.string.settings_photo_error, Toast.LENGTH_LONG).show()
                                }
                                return@Thread
                            }
                        }
                    }

                    val result = taskApi.addTimeSpent(
                        token,
                        taskId,
                        hours,
                        currentDateString(),
                        observation,
                        photoPath
                    )
                    mainHandler.post {
                        isSaving = false
                        when (result) {
                            TaskMutationResult.Success -> {
                                Toast.makeText(context, R.string.task_detail_updated, Toast.LENGTH_LONG).show()
                                loadTask()
                            }
                            TaskMutationResult.Unauthorized -> authFailure()
                            TaskMutationResult.NetworkError -> {
                                Toast.makeText(context, R.string.task_detail_error, Toast.LENGTH_LONG).show()
                            }
                            is TaskMutationResult.ServerError -> {
                                val message = result.message?.takeIf { it.isNotBlank() }
                                    ?: context.getString(R.string.task_detail_error)
                                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }.start()
                }
            }
        )
    }

    if (showAssigneePicker) {
        UserPickerDialog(
            title = stringResource(R.string.task_detail_add_assignee),
            users = assigneePickerUsers,
            emptyMessage = stringResource(R.string.project_no_users_available),
            onDismiss = { showAssigneePicker = false },
            onUserSelected = { user ->
                showAssigneePicker = false
                mutate(
                    action = { token -> taskApi.addAssignee(token, taskId, user.userId) },
                    successMessage = R.string.task_assignee_updated
                )
            }
        )
    }

    val assigneeMembers = task?.userIds.orEmpty().mapNotNull { userId ->
        projectMembers.firstOrNull { it.userId == userId }?.toProjectMember(
            index = projectMembers.indexOfFirst { it.userId == userId }.coerceAtLeast(0),
            isManager = userId == projectManagerId
        )
    }

    SubPageScaffold {
        SubPageContent {
            PageTopBar(
                title = task?.title ?: stringResource(R.string.task_detail_title),
                subtitle = if (isLoading) "" else taskStatusLabel(task?.status),
                onBack = { activity.finish() }
            )
            if (isLoading) {
                TaskDetailScreenSkeleton()
            } else {
            StatusPill(
                status = task?.status ?: "pending",
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TimeSpentSummaryCard(
                timeSpent = task?.timeSpent ?: 0.0,
                estimatedTime = task?.estimatedTime ?: 0.0
            )

            DetailSectionHeader(
                title = stringResource(R.string.task_detail_time_entries),
                count = timeEntries.size,
                topPadding = 18.dp,
                onAddClick = if (task?.status != "completed" && !isSaving) {
                    { showAddTimeDialog = true }
                } else {
                    null
                }
            )
            if (timeEntries.isEmpty()) {
                EmptyStateCard(text = stringResource(R.string.task_detail_no_time_entries))
            } else {
                timeEntries.forEach { entry ->
                    TimeSpentEntryCard(
                        entry = entry,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
            }

            if (!task?.description.isNullOrBlank()) {
                FormSection(R.string.task_detail_description) {
                    Text(
                        text = task?.description.orEmpty(),
                        color = colorResource(R.color.dashboard_text_primary),
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
            }

            DetailSectionHeader(
                title = stringResource(R.string.task_detail_assignee),
                count = assigneeMembers.size,
                topPadding = 22.dp,
                onAddClick = if (canManageTasks) {
                    {
                        val assignedIds = task?.userIds.orEmpty().toSet()
                        assigneePickerUsers = projectMembers.filter { member ->
                            member.active && member.userId !in assignedIds
                        }
                        showAssigneePicker = true
                    }
                } else {
                    null
                }
            )
            if (assigneeMembers.isEmpty()) {
                EmptyStateCard(text = stringResource(R.string.task_detail_no_assignee))
            } else {
                assigneeMembers.forEach { member ->
                    TeamMemberCard(
                        member = member,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        onRemove = if (canManageTasks && assigneeMembers.size > 1) {
                            {
                                mutate(
                                    action = { token ->
                                        taskApi.removeAssignee(token, taskId, member.userId)
                                    },
                                    successMessage = R.string.task_assignee_removed
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
            }

            if (task?.status != "completed") {
                FilledActionButton(
                    text = stringResource(R.string.task_detail_complete),
                    colorResId = R.color.project_green,
                    radius = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp)
                        .height(50.dp),
                    onClick = {
                        if (isSaving) return@FilledActionButton
                        showCompleteTaskConfirm = true
                    }
                )
            }
            if (canManageTasks) {
                OutlineActionButton(
                    text = stringResource(R.string.task_detail_delete),
                    colorResId = R.color.settings_logout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .height(46.dp),
                    onClick = {
                        if (isSaving) return@OutlineActionButton
                        showDeleteTaskConfirm = true
                    }
                )
            }
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
    var showCreateDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<ApiUser?>(null) }
    var userToChangePassword by remember { mutableStateOf<ApiUser?>(null) }
    var userToDelete by remember { mutableStateOf<ApiUser?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

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
                isLoading = false
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
    val activeCount = users.count { it.active }

    AppScaffold(selectedDestination = Destination.ADMIN) {
        if (isLoading) {
            AdminScreenSkeleton(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .screenContentPadding(bottom = 24.dp)
            )
        } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .screenContentPadding(bottom = 24.dp)
        ) {
            ScreenHeader(
                title = stringResource(R.string.admin_title),
                subtitle = stringResource(R.string.admin_subtitle)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminMetricCard(
                    label = stringResource(R.string.admin_users),
                    value = users.size.toString(),
                    iconResId = R.drawable.ic_settings,
                    accentColorResId = R.color.bottom_nav_selected,
                    iconBackgroundColorResId = R.color.task_blue_soft,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    label = stringResource(R.string.admin_metric_active),
                    value = activeCount.toString(),
                    iconResId = R.drawable.ic_check_circle,
                    accentColorResId = R.color.project_green,
                    iconBackgroundColorResId = R.color.project_green_soft,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminMetricCard(
                    label = stringResource(R.string.admin_metric_admins),
                    value = adminCount.toString(),
                    iconResId = R.drawable.ic_admin,
                    accentColorResId = R.color.project_purple,
                    iconBackgroundColorResId = R.color.project_purple_soft,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    label = stringResource(R.string.admin_metric_managers),
                    value = managerCount.toString(),
                    iconResId = R.drawable.ic_projects,
                    accentColorResId = R.color.task_orange,
                    iconBackgroundColorResId = R.color.task_orange_soft,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.admin_users),
                    modifier = Modifier.weight(1f),
                    color = colorResource(R.color.dashboard_text_primary),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                FilledActionButton(
                    text = stringResource(R.string.admin_create_user),
                    colorResId = R.color.login_button,
                    radius = 8.dp,
                    modifier = Modifier
                        .height(40.dp)
                        .widthIn(min = 120.dp),
                    onClick = { showCreateDialog = true }
                )
            }

            if (users.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp)
                        .cardBackground(R.color.dashboard_card, R.color.dashboard_card_stroke, 12.dp)
                        .padding(horizontal = 20.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.admin_no_users),
                        color = colorResource(R.color.dashboard_text_primary),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.admin_no_users_hint),
                        modifier = Modifier.padding(top = 6.dp),
                        color = colorResource(R.color.dashboard_text_secondary),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    users.forEach { user ->
                        AdminUserRow(
                            user = user,
                            canDelete = user.userId != currentUser?.userId,
                            modifier = Modifier.fillMaxWidth(),
                            onEdit = { userToEdit = user },
                            onChangePassword = { userToChangePassword = user },
                            onDelete = { userToDelete = user }
                        )
                    }
                }
            }
        }
        }
    }

    if (showCreateDialog) {
        AdminUserFormDialog(
            user = null,
            isSaving = isSaving,
            onDismiss = { if (!isSaving) showCreateDialog = false },
            onSave = { name, username, email, password, role, active ->
                if (isSaving) return@AdminUserFormDialog
                isSaving = true
                mutate(
                    mutation = { token ->
                        adminApi.createUser(
                            token,
                            CreateUserInput(
                                name = name,
                                username = username,
                                email = email,
                                password = password,
                                role = role,
                                active = active
                            )
                        )
                    },
                    successMessageResId = R.string.admin_create_success,
                    onSuccess = { showCreateDialog = false },
                    onFinished = { isSaving = false }
                )
            }
        )
    }

    userToEdit?.let { user ->
        AdminUserFormDialog(
            user = user,
            isSaving = isSaving,
            onDismiss = { if (!isSaving) userToEdit = null },
            onSave = { name, username, email, _, role, active ->
                if (isSaving) return@AdminUserFormDialog
                isSaving = true
                mutate(
                    mutation = { token ->
                        adminApi.updateUser(
                            token,
                            user.userId,
                            UpdateUserInput(
                                name = name,
                                username = username,
                                email = email,
                                role = role,
                                active = active
                            )
                        )
                    },
                    successMessageResId = R.string.admin_update_success,
                    onSuccess = { userToEdit = null },
                    onFinished = { isSaving = false }
                )
            }
        )
    }

    userToChangePassword?.let { user ->
        AdminChangePasswordDialog(
            userName = user.name,
            isSaving = isSaving,
            onDismiss = { if (!isSaving) userToChangePassword = null },
            onSave = { password ->
                if (isSaving) return@AdminChangePasswordDialog
                isSaving = true
                mutate(
                    mutation = { token -> adminApi.changePassword(token, user.userId, password) },
                    successMessageResId = R.string.admin_password_success,
                    onSuccess = { userToChangePassword = null },
                    onFinished = { isSaving = false }
                )
            }
        )
    }

    userToDelete?.let { user ->
        ConfirmActionDialog(
            title = stringResource(R.string.confirm_delete_title),
            message = stringResource(R.string.confirm_delete_user_message),
            confirmText = stringResource(R.string.admin_delete_user),
            onDismiss = { userToDelete = null },
            onConfirm = {
                userToDelete = null
                mutate(
                    mutation = { token -> adminApi.deleteUser(token, user.userId) },
                    successMessageResId = R.string.admin_delete_success,
                    onSuccess = {
                        if (userToEdit?.userId == user.userId) {
                            userToEdit = null
                        }
                    }
                )
            }
        )
    }
}

@Composable
fun StatisticsScreen() {
    val context = LocalContext.current
    val activity = context.findActivity()
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val statisticsApi = remember { StatisticsApi() }
    val projectsApi = remember { ProjectsApi() }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    var users by remember { mutableStateOf<List<ApiUser>>(emptyList()) }
    var projects by remember { mutableStateOf<List<ApiProject>>(emptyList()) }
    var category by remember { mutableStateOf(StatisticsCategory.USERS) }
    var selectedUserLabel by remember { mutableStateOf("") }
    var selectedProjectLabel by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isExporting by remember { mutableStateOf(false) }

    fun handleAuthFailure() {
        sessionManager.clear()
        activity.startActivity(Intent(activity, MainActivity::class.java))
        activity.finish()
    }

    fun loadData() {
        val token = sessionManager.token()
        if (token.isNullOrBlank()) {
            handleAuthFailure()
            return
        }

        Thread {
            val usersResult = statisticsApi.users(token)
            val projectsResult = projectsApi.listProjects(token)
            mainHandler.post {
                isLoading = false
                when (usersResult) {
                    is StatisticsUsersResult.Success -> {
                        users = usersResult.users
                        selectedUserLabel = usersResult.users.firstOrNull()?.label().orEmpty()
                    }
                    StatisticsUsersResult.Unauthorized -> handleAuthFailure()
                    StatisticsUsersResult.Forbidden -> {
                        Toast.makeText(context, R.string.statistics_forbidden, Toast.LENGTH_LONG).show()
                        activity.finish()
                    }
                    StatisticsUsersResult.NetworkError,
                    is StatisticsUsersResult.ServerError -> {
                        Toast.makeText(context, R.string.statistics_error, Toast.LENGTH_LONG).show()
                    }
                }
                when (projectsResult) {
                    is ProjectsResult.Success -> {
                        projects = projectsResult.projects
                        selectedProjectLabel = projectsResult.projects.firstOrNull()?.name.orEmpty()
                    }
                    ProjectsResult.Unauthorized -> handleAuthFailure()
                    ProjectsResult.NetworkError,
                    is ProjectsResult.ServerError -> {
                        if (usersResult !is StatisticsUsersResult.Forbidden) {
                            Toast.makeText(context, R.string.statistics_error, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }.start()
    }

    fun exportReport() {
        if (isExporting) return

        val token = sessionManager.token()
        if (token.isNullOrBlank()) {
            handleAuthFailure()
            return
        }

        val resultProvider: (String) -> StatisticsExportResult = when (category) {
            StatisticsCategory.USERS -> {
                val userId = users.firstOrNull { it.label() == selectedUserLabel }?.userId
                if (userId == null) {
                    Toast.makeText(context, R.string.statistics_select_target, Toast.LENGTH_LONG).show()
                    return
                }
                { statisticsApi.exportUserReport(it, userId) }
            }
            StatisticsCategory.PROJECTS,
            StatisticsCategory.PROJECT_TASKS -> {
                val projectId = projects.firstOrNull { it.name == selectedProjectLabel }?.projectId
                if (projectId == null) {
                    Toast.makeText(context, R.string.statistics_select_target, Toast.LENGTH_LONG).show()
                    return
                }
                if (category == StatisticsCategory.PROJECTS) {
                    { statisticsApi.exportProjectReport(it, projectId) }
                } else {
                    { statisticsApi.exportProjectTasksReport(it, projectId) }
                }
            }
        }

        isExporting = true
        Thread {
            val result = resultProvider(token)
            mainHandler.post {
                when (result) {
                    is StatisticsExportResult.Success -> {
                        val uri = savePdfToDownloads(context, result.bytes, result.filename)
                        if (uri == null) {
                            Toast.makeText(context, R.string.statistics_error, Toast.LENGTH_LONG).show()
                        } else {
                            val opened = openExportedPdf(context, uri)
                            Toast.makeText(
                                context,
                                if (opened) R.string.statistics_export_success else R.string.statistics_export_no_viewer,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    StatisticsExportResult.Unauthorized -> handleAuthFailure()
                    StatisticsExportResult.Forbidden -> Toast.makeText(context, R.string.statistics_forbidden, Toast.LENGTH_LONG).show()
                    StatisticsExportResult.NetworkError,
                    is StatisticsExportResult.ServerError -> Toast.makeText(context, R.string.statistics_error, Toast.LENGTH_LONG).show()
                }
                isExporting = false
            }
        }.start()
    }

    LaunchedEffect(Unit) {
        if (!sessionManager.canManageProjects()) {
            Toast.makeText(context, R.string.statistics_forbidden, Toast.LENGTH_LONG).show()
            activity.finish()
            return@LaunchedEffect
        }
        loadData()
    }

    val categoryOptions = listOf(
        stringResource(R.string.statistics_category_users),
        stringResource(R.string.statistics_category_projects),
        stringResource(R.string.statistics_category_project_tasks)
    )
    val categoryLabel = when (category) {
        StatisticsCategory.USERS -> categoryOptions[0]
        StatisticsCategory.PROJECTS -> categoryOptions[1]
        StatisticsCategory.PROJECT_TASKS -> categoryOptions[2]
    }
    val userOptions = users.map { it.label() }
    val projectOptions = projects.map { it.name }

    SubPageScaffold {
        SubPageContent {
            PageTopBar(
                title = stringResource(R.string.statistics_title),
                subtitle = stringResource(R.string.statistics_subtitle),
                onBack = { activity.finish() }
            )
            if (isLoading) {
                CreateFormScreenSkeleton()
            } else {
                FormSection(R.string.statistics_section_type) {
                    CreateTaskLabelNoTop(R.string.statistics_category)
                    SelectInput(
                        selected = categoryLabel,
                        values = categoryOptions,
                        onSelected = { label ->
                            category = when (label) {
                                categoryOptions[1] -> StatisticsCategory.PROJECTS
                                categoryOptions[2] -> StatisticsCategory.PROJECT_TASKS
                                else -> StatisticsCategory.USERS
                            }
                        }
                    )
                }
                FormSection(R.string.statistics_section_target) {
                    when (category) {
                        StatisticsCategory.USERS -> {
                            CreateTaskLabelNoTop(R.string.statistics_select_user)
                            if (userOptions.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.statistics_no_users),
                                    modifier = Modifier.padding(top = 6.dp),
                                    color = colorResource(R.color.dashboard_text_secondary),
                                    fontSize = 13.sp
                                )
                            } else {
                                SelectInput(
                                    selected = selectedUserLabel.ifBlank { userOptions.first() },
                                    values = userOptions,
                                    onSelected = { selectedUserLabel = it }
                                )
                            }
                        }
                        StatisticsCategory.PROJECTS,
                        StatisticsCategory.PROJECT_TASKS -> {
                            CreateTaskLabelNoTop(R.string.statistics_select_project)
                            if (projectOptions.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.statistics_no_projects),
                                    modifier = Modifier.padding(top = 6.dp),
                                    color = colorResource(R.color.dashboard_text_secondary),
                                    fontSize = 13.sp
                                )
                            } else {
                                SelectInput(
                                    selected = selectedProjectLabel.ifBlank { projectOptions.first() },
                                    values = projectOptions,
                                    onSelected = { selectedProjectLabel = it }
                                )
                            }
                        }
                    }
                }
                FilledActionButton(
                    text = stringResource(
                        if (isExporting) R.string.statistics_exporting else R.string.statistics_export
                    ),
                    colorResId = R.color.login_button,
                    radius = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .height(50.dp),
                    onClick = {
                        if (isExporting) return@FilledActionButton
                        val hasTarget = when (category) {
                            StatisticsCategory.USERS -> userOptions.isNotEmpty()
                            StatisticsCategory.PROJECTS,
                            StatisticsCategory.PROJECT_TASKS -> projectOptions.isNotEmpty()
                        }
                        if (!hasTarget) {
                            Toast.makeText(context, R.string.statistics_select_target, Toast.LENGTH_LONG).show()
                            return@FilledActionButton
                        }
                        exportReport()
                    }
                )
            }
        }
    }
}

private enum class StatisticsCategory {
    USERS,
    PROJECTS,
    PROJECT_TASKS
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val viewModel = remember { SettingsViewModel() }
    val sessionManager = remember { SessionManager(context.applicationContext) }
    val canManageProjects = remember { sessionManager.canManageProjects() }
    val authApi = remember { AuthApi() }
    val photoApi = remember { PhotoApi() }
    val userApi = remember { UserApi() }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    var currentUser by remember { mutableStateOf(sessionManager.currentUser()) }
    var isUploadingPhoto by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var language by remember { mutableStateOf(viewModel.getState(context).language) }
    val englishSelected = LocaleHelper.LANGUAGE_ENGLISH == language
    val currentLanguageName = stringResource(
        if (englishSelected) R.string.language_english else R.string.language_portuguese
    )
    val profileMember = remember(currentUser) {
        currentUser?.let { user ->
            ProjectMember(
                userId = user.userId,
                name = user.name,
                initials = user.name
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .take(2)
                    .joinToString("") { it.first().uppercase() }
                    .ifBlank { "U" },
                avatarColorResId = R.color.bottom_nav_selected,
                photo = user.photo
            )
        }
    }
    LaunchedEffect(Unit) {
        currentUser = sessionManager.currentUser()
        isLoading = false
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null || isUploadingPhoto) {
            return@rememberLauncherForActivityResult
        }
        val token = sessionManager.token()
        if (token.isNullOrBlank()) {
            return@rememberLauncherForActivityResult
        }

        isUploadingPhoto = true
        Thread {
            val mimeType = context.contentResolver.getType(uri).orEmpty().ifBlank { "image/jpeg" }
            val fileName = uri.lastPathSegment?.substringAfterLast('/')?.ifBlank { "profile.jpg" } ?: "profile.jpg"
            val bytes = runCatching {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }.getOrNull()

            if (bytes == null) {
                mainHandler.post {
                    isUploadingPhoto = false
                    Toast.makeText(context, R.string.settings_photo_error, Toast.LENGTH_LONG).show()
                }
                return@Thread
            }

            when (val uploadResult = photoApi.uploadPhoto(token, fileName, bytes, mimeType)) {
                is PhotoUploadResult.Success -> {
                    when (val profileResult = userApi.updateProfile(token, uploadResult.path)) {
                        is ProfileUpdateResult.Success -> {
                            sessionManager.updateUser(profileResult.user)
                            mainHandler.post {
                                currentUser = profileResult.user
                                isUploadingPhoto = false
                                Toast.makeText(context, R.string.settings_photo_updated, Toast.LENGTH_LONG).show()
                            }
                        }
                        ProfileUpdateResult.Unauthorized -> mainHandler.post {
                            isUploadingPhoto = false
                            sessionManager.clear()
                            restartRoot(context, MainActivity::class.java)
                        }
                        ProfileUpdateResult.NetworkError,
                        is ProfileUpdateResult.ServerError -> mainHandler.post {
                            isUploadingPhoto = false
                            Toast.makeText(context, R.string.settings_photo_error, Toast.LENGTH_LONG).show()
                        }
                    }
                }
                PhotoUploadResult.Unauthorized -> mainHandler.post {
                    isUploadingPhoto = false
                    sessionManager.clear()
                    restartRoot(context, MainActivity::class.java)
                }
                PhotoUploadResult.NetworkError,
                is PhotoUploadResult.ServerError -> mainHandler.post {
                    isUploadingPhoto = false
                    Toast.makeText(context, R.string.settings_photo_error, Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    AppScaffold(selectedDestination = Destination.SETTINGS) {
        if (isLoading) {
            SettingsScreenSkeleton(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .screenContentPadding(bottom = 24.dp)
            )
        } else {
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
                    text = stringResource(R.string.settings_profile_photo),
                    color = colorResource(R.color.dashboard_text_primary),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.settings_profile_photo_description),
                    modifier = Modifier.padding(top = 4.dp),
                    color = colorResource(R.color.dashboard_text_secondary),
                    fontSize = 14.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (profileMember != null) {
                        Avatar(
                            member = profileMember,
                            size = 72.dp
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp)
                    ) {
                        Text(
                            text = currentUser?.name ?: stringResource(R.string.settings_unknown_user),
                            color = colorResource(R.color.dashboard_text_primary),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(
                                if (isUploadingPhoto) {
                                    R.string.settings_uploading_photo
                                } else {
                                    R.string.settings_change_photo
                                }
                            ),
                            modifier = Modifier.padding(top = 4.dp),
                            color = colorResource(R.color.dashboard_text_secondary),
                            fontSize = 14.sp
                        )
                    }
                }
                OutlinedActionButton(
                    text = stringResource(
                        if (isUploadingPhoto) R.string.settings_uploading_photo else R.string.settings_change_photo
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .height(46.dp),
                    enabled = !isUploadingPhoto,
                    onClick = { photoPickerLauncher.launch("image/*") }
                )
            }
            if (canManageProjects) {
                SettingsPanel(Modifier.padding(top = 14.dp)) {
                    Text(
                        text = stringResource(R.string.statistics_title),
                        color = colorResource(R.color.dashboard_text_primary),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.statistics_settings_description),
                        modifier = Modifier.padding(top = 4.dp),
                        color = colorResource(R.color.dashboard_text_secondary),
                        fontSize = 14.sp
                    )
                    FilledActionButton(
                        text = stringResource(R.string.statistics_open),
                        colorResId = R.color.login_button,
                        radius = 8.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                            .height(46.dp),
                        onClick = {
                            context.startActivity(Intent(context, StatisticsActivity::class.java))
                        }
                    )
                }
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
                        language = LocaleHelper.LANGUAGE_ENGLISH,
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
                        language = LocaleHelper.LANGUAGE_PORTUGUESE,
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
                iconResId = R.drawable.ic_admin,
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
private fun LanguageFlagIcon(
    language: String,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp
) {
    val context = LocalContext.current
    var flagUrl by remember(language) { mutableStateOf(LanguageFlagStore.getFlagUrl(language)) }

    LaunchedEffect(language) {
        flagUrl = LanguageFlagStore.refreshFlags()[language] ?: flagUrl
    }

    flagUrl?.let { url ->
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(url)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(2.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun LanguageSelector(
    modifier: Modifier = Modifier,
    onLanguageSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val currentLanguage = LocaleHelper.getSavedLanguage(context)
    val languageCode = if (currentLanguage == LocaleHelper.LANGUAGE_ENGLISH) {
        "EN"
    } else {
        "PT"
    }
    var expanded by remember { mutableStateOf(false) }
    BoxWithConstraints(modifier = modifier) {
        Row(
            modifier = Modifier
                .height(32.dp)
                .clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(painterResource(R.drawable.ic_globe), contentDescription = null, modifier = Modifier.size(18.dp))
            LanguageFlagIcon(
                language = currentLanguage,
                modifier = Modifier.padding(start = 4.dp),
                size = 16.dp
            )
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
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(maxWidth),
            shape = RoundedCornerShape(12.dp),
            containerColor = colorResource(R.color.dashboard_card),
            tonalElevation = 0.dp,
            shadowElevation = 6.dp,
            border = BorderStroke(1.dp, colorResource(R.color.dashboard_card_stroke))
        ) {
            AppDropdownMenuItem(
                text = stringResource(R.string.language_english),
                leadingContent = {
                    LanguageFlagIcon(language = LocaleHelper.LANGUAGE_ENGLISH, size = 20.dp)
                },
                onClick = {
                    expanded = false
                    onLanguageSelected(LocaleHelper.LANGUAGE_ENGLISH)
                }
            )
            AppDropdownMenuItem(
                text = stringResource(R.string.language_portuguese),
                leadingContent = {
                    LanguageFlagIcon(language = LocaleHelper.LANGUAGE_PORTUGUESE, size = 20.dp)
                },
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
    @ColorRes iconBackgroundColorResId: Int,
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
            CardIcon(
                iconResId = iconResId,
                containerSize = 36.dp,
                iconSize = 18.dp,
                cornerRadius = 10.dp,
                backgroundColorResId = iconBackgroundColorResId,
                tintColorResId = progressColorResId
            )
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
    @DrawableRes iconResId: Int,
    @ColorRes accentColorResId: Int,
    @ColorRes iconBackgroundColorResId: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(100.dp)
            .cardBackground(R.color.dashboard_card, R.color.dashboard_card_stroke, 10.dp)
            .padding(13.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label.uppercase(Locale.getDefault()),
                modifier = Modifier.weight(1f),
                color = colorResource(R.color.dashboard_text_secondary),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 13.sp
            )
            CardIcon(
                iconResId = iconResId,
                containerSize = 32.dp,
                iconSize = 16.dp,
                cornerRadius = 8.dp,
                backgroundColorResId = iconBackgroundColorResId,
                tintColorResId = accentColorResId
            )
        }
        Text(
            text = value,
            modifier = Modifier.padding(top = 10.dp),
            color = colorResource(R.color.dashboard_text_primary),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
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
        IconBubble(
            backgroundColorResId = task.iconBackgroundColorResId,
            iconResId = task.iconResId,
            iconTintColorResId = task.accentColorResId,
            size = 30.dp,
            iconSize = 16.dp,
            radius = 15.dp,
            modifier = Modifier.padding(start = 10.dp)
        )
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
                    text = stringResource(R.string.project_status, projectStatusLabel(project.status)),
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
            // Mantém a altura do card consistente mesmo com descrições curtas.
            minLines = 2,
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
            IconBubble(
                backgroundColorResId = task.iconBackgroundColorResId,
                iconResId = task.iconResId,
                iconTintColorResId = task.accentColorResId,
                size = 34.dp,
                iconSize = 18.dp,
                radius = 17.dp
            )
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

private fun savePdfToDownloads(context: Context, bytes: ByteArray, filename: String): Uri? {
    val safeName = filename.replace(Regex("[^A-Za-z0-9._-]"), "_")
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        savePdfToDownloadsMediaStore(context, bytes, safeName)
    } else {
        savePdfToDownloadsLegacy(context, bytes, safeName)
    }
}

private fun savePdfToDownloadsMediaStore(context: Context, bytes: ByteArray, safeName: String): Uri? {
    val resolver = context.contentResolver
    val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    val pending = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, safeName)
        put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
        put(MediaStore.Downloads.IS_PENDING, 1)
    }
    val uri = resolver.insert(collection, pending) ?: return null
    return try {
        resolver.openOutputStream(uri)?.use { it.write(bytes) }
            ?: return null
        val published = ContentValues().apply {
            put(MediaStore.Downloads.IS_PENDING, 0)
        }
        resolver.update(uri, published, null, null)
        uri
    } catch (_: Exception) {
        resolver.delete(uri, null, null)
        null
    }
}

@Suppress("DEPRECATION")
private fun savePdfToDownloadsLegacy(context: Context, bytes: ByteArray, safeName: String): Uri? {
    return try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        downloadsDir.mkdirs()
        val file = File(downloadsDir, safeName)
        file.writeBytes(bytes)
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    } catch (_: Exception) {
        null
    }
}

private fun openExportedPdf(context: Context, uri: Uri): Boolean {
    val viewFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
    val pdfIntent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(viewFlags)
    }
    if (pdfIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(pdfIntent)
        return true
    }
    return openPdfInBrowser(context, uri)
}

private fun openPdfInBrowser(context: Context, uri: Uri): Boolean {
    val viewFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
    val browserPackages = context.packageManager
        .queryIntentActivities(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://")),
            PackageManager.MATCH_DEFAULT_ONLY
        )
        .mapNotNull { it.activityInfo?.packageName }
        .distinct()

    for (packageName in browserPackages) {
        val browserIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            setPackage(packageName)
            addFlags(viewFlags)
        }
        if (browserIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(browserIntent)
            return true
        }
    }

    val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
        setData(uri)
        addFlags(viewFlags)
    }
    return if (fallbackIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(Intent.createChooser(fallbackIntent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } else {
        false
    }
}

@Composable
private fun AdminUserRow(
    user: ApiUser,
    canDelete: Boolean,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onChangePassword: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 76.dp)
            .cardBackground(R.color.dashboard_card, R.color.dashboard_card_stroke, 10.dp)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
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
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.name,
                    modifier = Modifier.weight(1f),
                    color = colorResource(R.color.dashboard_text_primary),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!user.active) {
                    Box(
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colorResource(R.color.settings_logout).copy(alpha = 0.12f))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.admin_inactive),
                            color = colorResource(R.color.settings_logout),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                RoleBadge(user.role, Modifier.padding(start = 6.dp))
            }
            Text(
                text = "@${user.username}",
                modifier = Modifier.padding(top = 4.dp),
                color = colorResource(R.color.dashboard_text_secondary),
                fontSize = 12.sp,
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
        }
        Box {
            IconButtonLike(
                iconResId = R.drawable.ic_settings,
                contentDescription = stringResource(R.string.admin_users),
                tint = colorResource(R.color.dashboard_text_secondary),
                modifier = Modifier.size(36.dp),
                onClick = { menuExpanded = true }
            )
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                shape = RoundedCornerShape(12.dp),
                containerColor = colorResource(R.color.dashboard_card),
                tonalElevation = 0.dp,
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, colorResource(R.color.dashboard_card_stroke))
            ) {
                AppDropdownMenuItem(
                    text = stringResource(R.string.admin_action_edit),
                    onClick = {
                        menuExpanded = false
                        onEdit()
                    }
                )
                AppDropdownMenuItem(
                    text = stringResource(R.string.admin_action_password),
                    onClick = {
                        menuExpanded = false
                        onChangePassword()
                    }
                )
                if (canDelete) {
                    AppDropdownMenuItem(
                        text = stringResource(R.string.admin_action_delete),
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminUserFormDialog(
    user: ApiUser?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, username: String, email: String, password: String, role: String, active: Boolean) -> Unit
) {
    val isEdit = user != null
    var name by remember(user?.userId) { mutableStateOf(user?.name.orEmpty()) }
    var username by remember(user?.userId) { mutableStateOf(user?.username.orEmpty()) }
    var email by remember(user?.userId) { mutableStateOf(user?.email.orEmpty()) }
    var password by remember(user?.userId) { mutableStateOf("") }
    var role by remember(user?.userId) { mutableStateOf(user?.role?.ifBlank { "user" } ?: "user") }
    var active by remember(user?.userId) { mutableStateOf(user?.active ?: true) }
    var requiredError by remember(user?.userId) { mutableStateOf(false) }

    val roleUserLabel = stringResource(R.string.admin_role_user)
    val roleManagerLabel = stringResource(R.string.admin_role_manager)
    val roleAdminLabel = stringResource(R.string.admin_role_admin)
    val activeLabel = stringResource(R.string.admin_active)
    val inactiveLabel = stringResource(R.string.admin_user_inactive)

    AppFormDialog(
        title = stringResource(if (isEdit) R.string.admin_edit_user else R.string.admin_create_user),
        subtitle = stringResource(if (isEdit) R.string.admin_edit_subtitle else R.string.admin_create_subtitle),
        onDismiss = onDismiss,
        confirmText = stringResource(if (isEdit) R.string.admin_update_user else R.string.admin_save_user),
        onConfirm = {
            if (isSaving) return@AppFormDialog
            if (
                name.trim().isEmpty() ||
                username.trim().isEmpty() ||
                email.trim().isEmpty() ||
                (!isEdit && password.length < 6)
            ) {
                requiredError = true
                return@AppFormDialog
            }
            onSave(name.trim(), username.trim(), email.trim(), password, role, if (isEdit) active else true)
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
        if (isEdit) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
                Column(Modifier.weight(1f)) {
                    CreateTaskLabelNoTop(R.string.admin_active)
                    SelectInput(
                        selected = if (active) activeLabel else inactiveLabel,
                        values = listOf(activeLabel, inactiveLabel),
                        onSelected = { selected -> active = selected == activeLabel }
                    )
                }
            }
        } else {
            CreateTaskLabel(R.string.admin_role)
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
        if (!isEdit) {
            CreateTaskLabel(R.string.admin_password)
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
        if (requiredError) {
            Text(
                text = stringResource(R.string.admin_required_error),
                modifier = Modifier.padding(top = 10.dp),
                color = colorResource(R.color.login_error),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun AdminChangePasswordDialog(
    userName: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (password: String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var requiredError by remember { mutableStateOf(false) }
    var mismatchError by remember { mutableStateOf(false) }

    AppFormDialog(
        title = stringResource(R.string.admin_change_password),
        subtitle = stringResource(R.string.admin_change_password_subtitle),
        onDismiss = onDismiss,
        confirmText = stringResource(R.string.admin_change_password),
        onConfirm = {
            if (isSaving) return@AppFormDialog
            if (password.length < 6) {
                requiredError = true
                mismatchError = false
                return@AppFormDialog
            }
            if (password != confirmPassword) {
                mismatchError = true
                requiredError = false
                return@AppFormDialog
            }
            onSave(password)
        }
    ) {
        Text(
            text = userName,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(colorResource(R.color.login_input_background))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            color = colorResource(R.color.dashboard_text_primary),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        CreateTaskLabel(R.string.admin_new_password)
        CreateTaskInput(
            value = password,
            onValueChange = {
                password = it
                requiredError = false
                mismatchError = false
            },
            singleLine = true,
            keyboardType = KeyboardType.Password,
            visualTransformation = PasswordVisualTransformation()
        )
        CreateTaskLabel(R.string.admin_confirm_password)
        CreateTaskInput(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                requiredError = false
                mismatchError = false
            },
            singleLine = true,
            keyboardType = KeyboardType.Password,
            visualTransformation = PasswordVisualTransformation()
        )
        if (requiredError) {
            Text(
                text = stringResource(R.string.admin_required_error),
                modifier = Modifier.padding(top = 10.dp),
                color = colorResource(R.color.login_error),
                fontSize = 12.sp
            )
        }
        if (mismatchError) {
            Text(
                text = stringResource(R.string.admin_password_mismatch),
                modifier = Modifier.padding(top = 10.dp),
                color = colorResource(R.color.login_error),
                fontSize = 12.sp
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

private fun parseDecimalInput(value: String): Double? {
    val normalized = value.trim().replace(',', '.')
    if (normalized.isEmpty()) {
        return null
    }
    return normalized.toDoubleOrNull()
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
    val photoUrl = member.photo.takeIf { it.isNotBlank() }?.let { PhotoApi().photoUrl(it) }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(colorResource(member.avatarColorResId))
            .border(2.dp, colorResource(R.color.white), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = member.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = member.initials,
                color = colorResource(R.color.white),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun IconBubble(
    @ColorRes backgroundColorResId: Int,
    @DrawableRes iconResId: Int,
    @ColorRes iconTintColorResId: Int,
    size: Dp,
    iconSize: Dp,
    radius: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(radius))
            .background(colorResource(backgroundColorResId)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(iconResId),
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(colorResource(iconTintColorResId))
        )
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
    minHeight: Dp? = null,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val fieldHeight = minHeight ?: if (singleLine) 48.dp else 140.dp
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(fieldHeight)
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
            textStyle = TextStyle(
                color = colorResource(R.color.dashboard_text_primary),
                fontSize = 15.sp,
                lineHeight = if (singleLine) 15.sp else 21.sp
            ),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = visualTransformation
        )
    }
}

@Composable
private fun ConfirmActionDialog(
    title: String,
    message: String,
    confirmText: String,
    @ColorRes confirmColorResId: Int = R.color.settings_logout,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(colorResource(R.color.dashboard_card))
                .border(1.dp, colorResource(R.color.dashboard_card_stroke), RoundedCornerShape(18.dp))
                .padding(20.dp)
        ) {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                color = colorResource(R.color.dashboard_text_primary),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                color = colorResource(R.color.dashboard_text_secondary),
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlineActionButton(
                    text = stringResource(android.R.string.cancel),
                    colorResId = R.color.dashboard_text_secondary,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    onClick = onDismiss
                )
                FilledActionButton(
                    text = confirmText,
                    colorResId = confirmColorResId,
                    radius = 12.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    onClick = onConfirm
                )
            }
        }
    }
}

@Composable
private fun EditProjectDialog(
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    status: String,
    statusOptions: List<String>,
    statusValues: List<String>,
    onStatusChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(colorResource(R.color.dashboard_card))
                .border(1.dp, colorResource(R.color.dashboard_card_stroke), RoundedCornerShape(18.dp))
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(R.string.project_edit),
                modifier = Modifier.fillMaxWidth(),
                color = colorResource(R.color.dashboard_text_primary),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.create_project_subtitle),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                color = colorResource(R.color.dashboard_text_secondary),
                fontSize = 14.sp,
                lineHeight = 19.sp
            )

            Column(Modifier.padding(top = 18.dp)) {
                CreateTaskLabelNoTop(R.string.create_project_name)
                CreateTaskInput(
                    value = name,
                    onValueChange = onNameChange,
                    singleLine = true
                )
                CreateTaskLabel(R.string.create_project_description)
                CreateTaskInput(
                    value = description,
                    onValueChange = onDescriptionChange,
                    singleLine = false
                )
                CreateTaskLabel(R.string.create_task_status)
                StatusChoiceInput(
                    selected = status,
                    labels = statusOptions,
                    values = statusValues,
                    onSelected = onStatusChange
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlineActionButton(
                    text = stringResource(android.R.string.cancel),
                    colorResId = R.color.dashboard_text_secondary,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    onClick = onDismiss
                )
                FilledActionButton(
                    text = stringResource(R.string.create_project_save),
                    colorResId = R.color.login_button,
                    radius = 12.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    onClick = onSave
                )
            }
        }
    }
}

@Composable
private fun AppDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    leadingContent: (@Composable () -> Unit)? = null
) {
    DropdownMenuItem(
        text = {
            if (leadingContent == null) {
                Text(
                    text = text,
                    color = colorResource(R.color.dashboard_text_primary),
                    fontSize = 15.sp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    leadingContent()
                    Text(
                        text = text,
                        color = colorResource(R.color.dashboard_text_primary),
                        fontSize = 15.sp
                    )
                }
            }
        },
        onClick = onClick,
        colors = MenuDefaults.itemColors(
            textColor = colorResource(R.color.dashboard_text_primary)
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    )
}

@Composable
private fun AppFormDialog(
    title: String,
    subtitle: String? = null,
    onDismiss: () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    @ColorRes confirmColorResId: Int = R.color.login_button,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(colorResource(R.color.dashboard_card))
                .border(1.dp, colorResource(R.color.dashboard_card_stroke), RoundedCornerShape(18.dp))
                .padding(20.dp)
        ) {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                color = colorResource(R.color.dashboard_text_primary),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    color = colorResource(R.color.dashboard_text_secondary),
                    fontSize = 14.sp,
                    lineHeight = 19.sp
                )
            }
            Column(Modifier.padding(top = 18.dp), content = content)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlineActionButton(
                    text = stringResource(android.R.string.cancel),
                    colorResId = R.color.dashboard_text_secondary,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    onClick = onDismiss
                )
                FilledActionButton(
                    text = confirmText,
                    colorResId = confirmColorResId,
                    radius = 12.dp,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    onClick = onConfirm
                )
            }
        }
    }
}

@Composable
private fun StatusChoiceInput(
    selected: String,
    labels: List<String>,
    values: List<String>,
    onSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        values.forEachIndexed { index, value ->
            val label = labels.getOrElse(index) { value }
            val isSelected = value.equals(selected, ignoreCase = true)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) {
                            colorResource(R.color.task_blue_soft)
                        } else {
                            colorResource(R.color.dashboard_background)
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = colorResource(
                            if (isSelected) R.color.bottom_nav_selected else R.color.dashboard_card_stroke
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelected(value) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .border(
                            width = 2.dp,
                            color = colorResource(
                                if (isSelected) R.color.bottom_nav_selected else R.color.dashboard_card_stroke
                            ),
                            shape = CircleShape
                        )
                        .background(
                            if (isSelected) colorResource(R.color.bottom_nav_selected) else Color.Transparent
                        )
                )
                Text(
                    text = label,
                    modifier = Modifier.padding(start = 12.dp),
                    color = colorResource(R.color.dashboard_text_primary),
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun SelectInput(
    selected: String,
    values: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .padding(top = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .inputBackground()
                .clickable { expanded = true }
                .padding(start = 12.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selected,
                modifier = Modifier.weight(1f),
                color = colorResource(R.color.dashboard_text_primary),
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Image(
                painter = painterResource(R.drawable.ic_chevron_down),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                colorFilter = ColorFilter.tint(colorResource(R.color.dashboard_text_secondary))
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(maxWidth),
            shape = RoundedCornerShape(12.dp),
            containerColor = colorResource(R.color.dashboard_card),
            tonalElevation = 0.dp,
            shadowElevation = 6.dp,
            border = BorderStroke(1.dp, colorResource(R.color.dashboard_card_stroke))
        ) {
            values.forEach { value ->
                AppDropdownMenuItem(
                    text = value,
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
        Image(
            painter = painterResource(iconResId),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(colorResource(R.color.dashboard_text_secondary))
        )
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
    language: String,
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LanguageFlagIcon(language = language, size = 20.dp)
            Text(text = text, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun OutlinedActionButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val textColor = if (enabled) {
        colorResource(R.color.login_link)
    } else {
        colorResource(R.color.login_text_secondary)
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, colorResource(R.color.login_input_stroke), RoundedCornerShape(6.dp))
            .background(colorResource(R.color.login_input_background))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = text, color = textColor, fontSize = 16.sp)
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
