package com.example.commov.ui.projects;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.commov.R;
import com.example.commov.model.DashboardTask;
import com.example.commov.model.Project;
import com.example.commov.model.ProjectMember;
import com.example.commov.model.ProjectTask;
import com.example.commov.viewmodel.ProjectsUiState;
import com.example.commov.viewmodel.ProjectsViewModel;

public final class ProjectsScreen {
    private static final float AVATAR_STEP_RATIO = 0.75f;

    private final Activity activity;
    private final ProjectsViewModel viewModel = new ProjectsViewModel();
    private ProjectsUiState currentState;

    public ProjectsScreen(Activity activity) {
        this.activity = activity;
    }

    public void bind() {
        render(viewModel.getState());
    }

    private void render(ProjectsUiState state) {
        currentState = state;
        ((TextView) activity.findViewById(R.id.projectsTitle)).setText(R.string.projects_title);
        ((TextView) activity.findViewById(R.id.projectsSubtitle)).setText(R.string.projects_subtitle);

        LinearLayout projectsList = activity.findViewById(R.id.projectsList);
        projectsList.removeAllViews();

        for (int i = 0; i < state.projects.size(); i++) {
            projectsList.addView(createProjectView(projectsList, state.projects.get(i), i == 0));
        }
    }

    private View createProjectView(LinearLayout parent, Project project, boolean opensDetail) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_project_card, parent, false);

        FrameLayout badge = view.findViewById(R.id.projectBadge);
        TextView initials = view.findViewById(R.id.projectInitials);
        TextView name = view.findViewById(R.id.projectName);
        TextView taskCount = view.findViewById(R.id.projectTaskCount);
        TextView description = view.findViewById(R.id.projectDescription);
        FrameLayout membersAvatars = view.findViewById(R.id.projectMembersAvatars);

        badge.setBackground(makeRoundBackground(project.badgeColorResId, 21));
        initials.setText(project.initials);
        initials.setTextColor(ContextCompat.getColor(activity, project.accentColorResId));
        name.setText(project.nameResId);
        taskCount.setText(activity.getString(R.string.project_tasks_count, project.taskCount));
        description.setText(project.descriptionResId);
        renderMembers(membersAvatars, project);

        if (opensDetail) {
            view.setClickable(true);
            view.setOnClickListener(clickedView -> renderProjectDetail(project));
        }

        applyProjectCardWidth(view);
        return view;
    }

    private void renderProjectDetail(Project project) {
        ((TextView) activity.findViewById(R.id.projectsTitle)).setText(project.nameResId);
        ((TextView) activity.findViewById(R.id.projectsSubtitle)).setText(project.descriptionResId);

        LinearLayout projectsList = activity.findViewById(R.id.projectsList);
        projectsList.removeAllViews();
        projectsList.addView(createBackView());
        projectsList.addView(createCreateTaskButton(project));
        projectsList.addView(createSectionTitle(R.string.project_detail_tasks, 18));

        for (ProjectTask task : project.tasks) {
            projectsList.addView(createTaskView(task));
        }

        projectsList.addView(createSectionTitle(R.string.project_detail_people, 16));

        for (ProjectMember member : project.members) {
            projectsList.addView(createMemberView(member));
        }
    }

    private TextView createBackView() {
        TextView backView = new TextView(activity);
        backView.setText(R.string.project_detail_back);
        backView.setTextColor(ContextCompat.getColor(activity, R.color.bottom_nav_selected));
        backView.setTextSize(14);
        backView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        backView.setGravity(Gravity.CENTER_VERTICAL);
        backView.setOnClickListener(view -> render(currentState));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(36)
        );
        params.bottomMargin = dp(6);
        backView.setLayoutParams(params);
        return backView;
    }

    private Button createCreateTaskButton(Project project) {
        Button button = new Button(activity);
        button.setBackgroundResource(R.drawable.bg_login_button);
        button.setText(R.string.project_create_task);
        button.setTextColor(ContextCompat.getColor(activity, R.color.white));
        button.setTextSize(15);
        button.setOnClickListener(view -> {
            Intent intent = new Intent(activity, CreateTaskActivity.class);
            intent.putExtra(CreateTaskActivity.EXTRA_PROJECT_ID, project.projectId);
            intent.putExtra(CreateTaskActivity.EXTRA_PROJECT_NAME, activity.getString(project.nameResId));
            activity.startActivity(intent);
        });

        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        int width = Math.round(screenWidth * 0.90f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, dp(46));
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.topMargin = dp(6);
        params.bottomMargin = dp(8);
        button.setLayoutParams(params);
        return button;
    }

    private TextView createSectionTitle(int titleResId, int topMarginDp) {
        TextView title = new TextView(activity);
        title.setText(titleResId);
        title.setTextColor(ContextCompat.getColor(activity, R.color.dashboard_text_primary));
        title.setTextSize(18);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(topMarginDp);
        params.bottomMargin = dp(10);
        title.setLayoutParams(params);
        return title;
    }

    private View createTaskView(ProjectTask projectTask) {
        DashboardTask task = projectTask.task;
        LinearLayout card = new LinearLayout(activity);
        card.setBackground(makeRoundBackground(R.color.dashboard_card, 8));
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));

        LinearLayout topRow = new LinearLayout(activity);
        topRow.setGravity(Gravity.CENTER_VERTICAL);
        topRow.setOrientation(LinearLayout.HORIZONTAL);

        FrameLayout iconContainer = new FrameLayout(activity);
        iconContainer.setBackground(makeRoundBackground(task.iconBackgroundColorResId, 17));

        ImageView icon = new ImageView(activity);
        icon.setImageResource(task.iconResId);
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(dp(18), dp(18));
        iconParams.gravity = Gravity.CENTER;
        iconContainer.addView(icon, iconParams);

        LinearLayout.LayoutParams iconContainerParams = new LinearLayout.LayoutParams(dp(34), dp(34));
        topRow.addView(iconContainer, iconContainerParams);

        LinearLayout textGroup = new LinearLayout(activity);
        textGroup.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(activity);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        title.setMaxLines(1);
        title.setText(task.titleResId);
        title.setTextColor(ContextCompat.getColor(activity, R.color.dashboard_text_primary));
        title.setTextSize(15);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        TextView meta = new TextView(activity);
        meta.setEllipsize(android.text.TextUtils.TruncateAt.END);
        meta.setMaxLines(1);
        meta.setText(task.metaResId);
        meta.setTextColor(ContextCompat.getColor(activity, R.color.dashboard_text_secondary));
        meta.setTextSize(12);

        textGroup.addView(title);
        textGroup.addView(meta);

        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        textParams.leftMargin = dp(12);
        textParams.rightMargin = dp(8);
        topRow.addView(textGroup, textParams);

        TextView status = new TextView(activity);
        status.setGravity(Gravity.CENTER);
        status.setMinWidth(dp(58));
        status.setPadding(dp(8), 0, dp(8), 0);
        status.setText(task.statusResId);
        status.setAllCaps(true);
        status.setTextColor(ContextCompat.getColor(activity, task.statusTextColorResId));
        status.setTextSize(8);
        status.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        status.setBackground(makeRoundBackground(task.statusBackgroundColorResId, 14));

        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(22)
        );
        topRow.addView(status, statusParams);
        card.addView(topRow);

        FrameLayout assignees = new FrameLayout(activity);
        renderAssignees(assignees, projectTask.assignees);
        LinearLayout.LayoutParams assigneesParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(36)
        );
        assigneesParams.topMargin = dp(14);
        card.addView(assignees, assigneesParams);

        applyTaskWidth(card);
        return card;
    }

    private void renderAssignees(FrameLayout assigneesView, java.util.List<ProjectMember> assignees) {
        assigneesView.removeAllViews();

        int avatarSize = dp(34);
        int avatarStep = Math.round(avatarSize * AVATAR_STEP_RATIO);

        for (int i = 0; i < assignees.size(); i++) {
            ProjectMember member = assignees.get(i);
            TextView avatar = new TextView(activity);
            avatar.setBackground(makeAvatarBackground(member.avatarColorResId));
            avatar.setContentDescription(member.name);
            avatar.setGravity(Gravity.CENTER);
            avatar.setText(member.initials);
            avatar.setTextColor(ContextCompat.getColor(activity, R.color.white));
            avatar.setTextSize(11);
            avatar.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(avatarSize, avatarSize);
            params.leftMargin = i * avatarStep;
            params.gravity = Gravity.CENTER_VERTICAL;
            assigneesView.addView(avatar, params);
        }
    }

    private View createMemberView(ProjectMember member) {
        LinearLayout memberView = new LinearLayout(activity);
        memberView.setBackground(makeRoundBackground(R.color.dashboard_card, 8));
        memberView.setGravity(Gravity.CENTER_VERTICAL);
        memberView.setOrientation(LinearLayout.HORIZONTAL);
        memberView.setPadding(dp(12), 0, dp(12), 0);

        TextView avatar = new TextView(activity);
        avatar.setBackground(makeAvatarBackground(member.avatarColorResId));
        avatar.setGravity(Gravity.CENTER);
        avatar.setText(member.initials);
        avatar.setTextColor(ContextCompat.getColor(activity, R.color.white));
        avatar.setTextSize(11);
        avatar.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(34), dp(34));
        memberView.addView(avatar, avatarParams);

        TextView name = new TextView(activity);
        name.setText(member.name);
        name.setTextColor(ContextCompat.getColor(activity, R.color.dashboard_text_primary));
        name.setTextSize(14);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );
        nameParams.leftMargin = dp(12);
        memberView.addView(name, nameParams);

        applyMemberWidth(memberView);
        return memberView;
    }

    private void renderMembers(FrameLayout membersAvatars, Project project) {
        membersAvatars.removeAllViews();

        int avatarSize = dp(32);
        int avatarStep = Math.round(avatarSize * AVATAR_STEP_RATIO);

        for (int i = 0; i < project.members.size(); i++) {
            ProjectMember member = project.members.get(i);
            TextView avatar = new TextView(activity);
            avatar.setBackground(makeAvatarBackground(member.avatarColorResId));
            avatar.setContentDescription(member.name);
            avatar.setGravity(Gravity.CENTER);
            avatar.setText(member.initials);
            avatar.setTextColor(ContextCompat.getColor(activity, R.color.white));
            avatar.setTextSize(11);
            avatar.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(avatarSize, avatarSize);
            params.leftMargin = i * avatarStep;
            params.gravity = Gravity.CENTER_VERTICAL;
            membersAvatars.addView(avatar, params);
        }
    }

    private void applyProjectCardWidth(View view) {
        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        int width = Math.round(screenWidth * 0.90f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.bottomMargin = dp(14);
        view.setLayoutParams(params);
    }

    private void applyTaskWidth(View view) {
        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        int width = Math.round(screenWidth * 0.90f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, dp(112));
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.bottomMargin = dp(10);
        view.setLayoutParams(params);
    }

    private void applyMemberWidth(View view) {
        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        int width = Math.round(screenWidth * 0.90f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, dp(58));
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.bottomMargin = dp(8);
        view.setLayoutParams(params);
    }

    private GradientDrawable makeRoundBackground(int colorResId, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(ContextCompat.getColor(activity, colorResId));
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private GradientDrawable makeAvatarBackground(int colorResId) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(ContextCompat.getColor(activity, colorResId));
        drawable.setStroke(dp(2), ContextCompat.getColor(activity, R.color.white));
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
