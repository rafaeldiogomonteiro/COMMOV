package com.example.commov.ui.compose

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.shadow
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
import com.example.commov.data.local.LocaleHelper
import com.example.commov.model.DashboardTask
import com.example.commov.model.Project
import com.example.commov.model.ProjectMember
import com.example.commov.model.ProjectTask
import com.example.commov.ui.dashboard.DashboardActivity
import com.example.commov.ui.projects.CreateTaskActivity
import com.example.commov.ui.projects.ProjectsActivity
import com.example.commov.ui.settings.SettingsActivity
import com.example.commov.viewmodel.DashboardViewModel
import com.example.commov.viewmodel.LoginUiState
import com.example.commov.viewmodel.LoginViewModel
import com.example.commov.viewmodel.ProjectsViewModel
import com.example.commov.viewmodel.SettingsViewModel
import java.util.Calendar
import java.util.Locale

enum class Destination {
    HOME,
    PROJECTS,
    SETTINGS
}

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val activity = context.findActivity()
    val viewModel = remember { LoginViewModel() }
    var state by remember { mutableStateOf(LoginUiState("", "", false, 0, 0, false)) }

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
            .background(colorResource(R.color.login_background)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 14.dp)
                .fillMaxWidth()
                .shadow(14.dp, RoundedCornerShape(10.dp), clip = false)
                .cardBackground(R.color.login_card, R.color.login_card_stroke, 10.dp)
                .padding(start = 32.dp, top = 40.dp, end = 32.dp, bottom = 38.dp)
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
                text = stringResource(R.string.login_title),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 26.dp),
                color = colorResource(R.color.login_text_primary),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.login_subtitle),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                color = colorResource(R.color.login_text_secondary),
                fontSize = 16.sp,
                lineHeight = 20.sp
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.login_password_label),
                    modifier = Modifier.weight(1f),
                    color = colorResource(R.color.login_text_secondary),
                    fontSize = 16.sp
                )
                Text(
                    text = stringResource(R.string.login_forgot_password),
                    modifier = Modifier.clickable {
                        Toast.makeText(context, R.string.login_forgot_password, Toast.LENGTH_SHORT).show()
                    },
                    color = colorResource(R.color.login_link),
                    fontSize = 16.sp
                )
            }
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
            FilledActionButton(
                text = stringResource(R.string.login_button),
                iconResId = R.drawable.ic_arrow_right,
                colorResId = R.color.login_button,
                radius = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .height(48.dp),
                onClick = viewModel::onLoginClicked
            )
        }
    }
}

@Composable
fun DashboardScreen() {
    val state = remember { DashboardViewModel().state }

    AppScaffold(selectedDestination = Destination.HOME) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 18.dp, top = 38.dp, end = 18.dp, bottom = 20.dp)
        ) {
            Spacer(Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.dashboard_greeting, state.userName),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
                color = colorResource(R.color.dashboard_text_primary),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp),
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
                    progressColorResId = R.color.dashboard_muted,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 34.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_my_tasks),
                    modifier = Modifier.weight(1f),
                    color = colorResource(R.color.dashboard_text_primary),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.dashboard_see_all),
                    modifier = Modifier.height(36.dp),
                    color = colorResource(R.color.dashboard_text_primary),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                state.tasks.forEach { task ->
                    DashboardTaskCard(
                        task = task,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(66.dp)
                            .padding(bottom = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectsScreen() {
    val context = LocalContext.current
    val state = remember { ProjectsViewModel().state }
    var selectedProject by remember { mutableStateOf<Project?>(null) }
    val project = selectedProject

    AppScaffold(selectedDestination = Destination.PROJECTS) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 18.dp, top = 66.dp, end = 18.dp, bottom = 20.dp)
        ) {
            Text(
                text = if (project == null) {
                    stringResource(R.string.projects_title)
                } else {
                    stringResource(project.nameResId)
                },
                modifier = Modifier.fillMaxWidth(),
                color = colorResource(R.color.dashboard_text_primary),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (project == null) {
                    stringResource(R.string.projects_subtitle)
                } else {
                    stringResource(project.descriptionResId)
                },
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
                if (project == null) {
                    state.projects.forEachIndexed { index, item ->
                        ProjectCard(
                            project = item,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp),
                            onClick = if (index == 0) {
                                { selectedProject = item }
                            } else {
                                null
                            }
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.project_detail_back),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .clickable { selectedProject = null },
                        color = colorResource(R.color.bottom_nav_selected),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    FilledActionButton(
                        text = stringResource(R.string.project_create_task),
                        colorResId = R.color.login_button,
                        radius = 6.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 8.dp)
                            .height(46.dp),
                        onClick = {
                            val intent = Intent(context, CreateTaskActivity::class.java)
                            intent.putExtra(CreateTaskActivity.EXTRA_PROJECT_ID, project.projectId)
                            intent.putExtra(CreateTaskActivity.EXTRA_PROJECT_NAME, context.getString(project.nameResId))
                            context.startActivity(intent)
                        }
                    )
                    SectionTitle(R.string.project_detail_tasks, topPadding = 18.dp)
                    project.tasks.forEach { task ->
                        ProjectTaskCard(
                            projectTask = task,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(112.dp)
                                .padding(bottom = 10.dp)
                        )
                    }
                    SectionTitle(R.string.project_detail_people, topPadding = 16.dp)
                    project.members.forEach { member ->
                        MemberRow(
                            member = member,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateTaskScreen(projectName: String?) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val resolvedProjectName = projectName?.takeIf { it.trim().isNotEmpty() }
        ?: stringResource(R.string.project_alpha_name)
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var assignee by remember { mutableStateOf("Ricardo Silva") }
    var status by remember { mutableStateOf("pending") }
    var estimatedEndDate by remember { mutableStateOf("") }
    var workDate by remember { mutableStateOf("") }
    var actualEndDate by remember { mutableStateOf("") }
    var estimatedTime by remember { mutableStateOf("") }
    var completionRate by remember { mutableStateOf("") }
    var timeSpent by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var observation by remember { mutableStateOf("") }
    var photo by remember { mutableStateOf("") }
    var titleError by remember { mutableStateOf(false) }

    AppScaffold(selectedDestination = Destination.PROJECTS) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 18.dp, top = 56.dp, end = 18.dp, bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.create_task_title),
                modifier = Modifier.fillMaxWidth(),
                color = colorResource(R.color.dashboard_text_primary),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.create_task_project) + ": " + resolvedProjectName,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                color = colorResource(R.color.dashboard_text_secondary),
                fontSize = 15.sp
            )
            CreateTaskLabel(R.string.create_task_task_title)
            CreateTaskInput(
                value = title,
                onValueChange = {
                    title = it
                    titleError = false
                },
                singleLine = true,
                keyboardType = KeyboardType.Text
            )
            if (titleError) {
                Text(
                    text = stringResource(R.string.create_task_title_error),
                    modifier = Modifier.padding(top = 4.dp),
                    color = colorResource(R.color.login_error),
                    fontSize = 12.sp
                )
            }
            CreateTaskLabel(R.string.create_task_user)
            SelectInput(
                selected = assignee,
                values = listOf("Ricardo Silva", "Ana Costa", "Marta Reis", "Joao Lima"),
                onSelected = { assignee = it }
            )
            CreateTaskLabel(R.string.create_task_status)
            SelectInput(
                selected = status,
                values = listOf("pending", "in_progress", "completed", "blocked"),
                onSelected = { status = it }
            )
            CreateTaskLabel(R.string.create_task_description)
            CreateTaskInput(
                value = description,
                onValueChange = { description = it },
                minHeight = 92.dp,
                singleLine = false
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    CreateTaskLabelNoTop(R.string.create_task_estimated_end_date)
                    PickerInput(
                        value = estimatedEndDate,
                        hint = stringResource(R.string.create_task_date_hint),
                        iconResId = R.drawable.ic_calendar_clock,
                        onClick = { showDatePicker(context) { estimatedEndDate = it } }
                    )
                }
                Column(Modifier.weight(1f)) {
                    CreateTaskLabelNoTop(R.string.create_task_work_date)
                    PickerInput(
                        value = workDate,
                        hint = stringResource(R.string.create_task_date_hint),
                        iconResId = R.drawable.ic_calendar_clock,
                        onClick = { showDatePicker(context) { workDate = it } }
                    )
                }
            }
            CreateTaskLabel(R.string.create_task_actual_end_date)
            PickerInput(
                value = actualEndDate,
                hint = stringResource(R.string.create_task_date_hint),
                iconResId = R.drawable.ic_calendar_clock,
                onClick = { showDatePicker(context) { actualEndDate = it } }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    CreateTaskLabelNoTop(R.string.create_task_estimated_time)
                    PickerInput(
                        value = estimatedTime,
                        hint = stringResource(R.string.create_task_number_hint),
                        iconResId = R.drawable.ic_clock,
                        onClick = { showTimePicker(context) { estimatedTime = it } }
                    )
                }
                Column(Modifier.weight(1f)) {
                    CreateTaskLabelNoTop(R.string.create_task_completion_rate)
                    PickerInput(
                        value = completionRate,
                        hint = stringResource(R.string.create_task_number_hint),
                        iconResId = R.drawable.ic_check_circle,
                        onClick = { showCompletionPicker(context) { completionRate = it } }
                    )
                }
            }
            CreateTaskLabel(R.string.create_task_time_spent)
            PickerInput(
                value = timeSpent,
                hint = stringResource(R.string.create_task_number_hint),
                iconResId = R.drawable.ic_clock,
                onClick = { showTimePicker(context) { timeSpent = it } }
            )
            CreateTaskLabel(R.string.create_task_location)
            CreateTaskInput(value = location, onValueChange = { location = it }, singleLine = true)
            CreateTaskLabel(R.string.create_task_observation)
            CreateTaskInput(
                value = observation,
                onValueChange = { observation = it },
                minHeight = 82.dp,
                singleLine = false
            )
            CreateTaskLabel(R.string.create_task_photo)
            CreateTaskInput(
                value = photo,
                onValueChange = { photo = it },
                singleLine = true,
                keyboardType = KeyboardType.Uri
            )
            FilledActionButton(
                text = stringResource(R.string.create_task_save),
                colorResId = R.color.login_button,
                radius = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .height(48.dp),
                onClick = {
                    if (title.trim().isEmpty()) {
                        titleError = true
                        return@FilledActionButton
                    }
                    Toast.makeText(context, R.string.create_task_saved, Toast.LENGTH_LONG).show()
                    activity.finish()
                }
            )
        }
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val viewModel = remember { SettingsViewModel() }
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
                .padding(start = 18.dp, top = 66.dp, end = 18.dp, bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                modifier = Modifier.fillMaxWidth(),
                color = colorResource(R.color.dashboard_text_primary),
                fontSize = 28.sp,
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
                    text = stringResource(R.string.settings_user_name),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    color = colorResource(R.color.dashboard_text_primary),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.settings_user_email),
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
                    onClick = { restartRoot(context, MainActivity::class.java) }
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
        Box(modifier = Modifier.weight(1f)) {
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
                text = stringResource(R.string.login_language),
                modifier = Modifier.padding(start = 4.dp),
                color = colorResource(R.color.login_text_secondary),
                fontSize = 16.sp
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
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
        }
        ProgressLine(progress = progress, colorResId = progressColorResId)
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
private fun DashboardTaskCard(task: DashboardTask, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .cardBackground(R.color.dashboard_card, R.color.dashboard_card_stroke, 8.dp)
            .clip(RoundedCornerShape(8.dp)),
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
                text = stringResource(task.titleResId),
                color = colorResource(R.color.dashboard_text_primary),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(task.metaResId),
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
                    text = stringResource(project.nameResId),
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
            }
        }
        Text(
            text = stringResource(project.descriptionResId),
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
private fun ProjectTaskCard(projectTask: ProjectTask, modifier: Modifier = Modifier) {
    val task = projectTask.task
    Column(
        modifier = modifier
            .cardBackground(R.color.dashboard_card, R.color.dashboard_card_stroke, 8.dp)
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
                    text = stringResource(task.titleResId),
                    color = colorResource(R.color.dashboard_text_primary),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(task.metaResId),
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
private fun MemberRow(member: ProjectMember, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
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
    }
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
            text = stringResource(task.statusResId).uppercase(Locale.getDefault()),
            color = colorResource(task.statusTextColorResId),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun SectionTitle(@StringRes titleResId: Int, topPadding: Dp) {
    Text(
        text = stringResource(titleResId),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding, bottom = 10.dp),
        color = colorResource(R.color.dashboard_text_primary),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )
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
    keyboardType: KeyboardType = KeyboardType.Text
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
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
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
