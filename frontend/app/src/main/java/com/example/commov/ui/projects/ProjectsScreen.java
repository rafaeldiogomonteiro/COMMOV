package com.example.commov.ui.projects;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.commov.R;
import com.example.commov.model.Project;
import com.example.commov.viewmodel.ProjectsUiState;
import com.example.commov.viewmodel.ProjectsViewModel;

public final class ProjectsScreen {
    private final Activity activity;
    private final ProjectsViewModel viewModel = new ProjectsViewModel();

    public ProjectsScreen(Activity activity) {
        this.activity = activity;
    }

    public void bind() {
        render(viewModel.getState());
    }

    private void render(ProjectsUiState state) {
        LinearLayout projectsList = activity.findViewById(R.id.projectsList);
        projectsList.removeAllViews();

        for (Project project : state.projects) {
            projectsList.addView(createProjectView(projectsList, project));
        }
    }

    private View createProjectView(LinearLayout parent, Project project) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_project_card, parent, false);

        FrameLayout badge = view.findViewById(R.id.projectBadge);
        TextView initials = view.findViewById(R.id.projectInitials);
        TextView name = view.findViewById(R.id.projectName);
        TextView taskCount = view.findViewById(R.id.projectTaskCount);
        TextView description = view.findViewById(R.id.projectDescription);
        ProgressBar progressBar = view.findViewById(R.id.projectProgressBar);
        TextView progressText = view.findViewById(R.id.projectProgressText);

        badge.setBackground(makeRoundBackground(project.badgeColorResId, 21));
        initials.setText(project.initials);
        initials.setTextColor(ContextCompat.getColor(activity, project.accentColorResId));
        name.setText(project.nameResId);
        taskCount.setText(activity.getString(R.string.project_tasks_count, project.taskCount));
        description.setText(project.descriptionResId);
        progressBar.setProgress(project.progress);
        progressText.setText(activity.getString(R.string.project_progress, project.progress));

        applyProjectCardWidth(view);
        return view;
    }

    private void applyProjectCardWidth(View view) {
        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        int width = Math.round(screenWidth * 0.90f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.bottomMargin = dp(14);
        view.setLayoutParams(params);
    }

    private GradientDrawable makeRoundBackground(int colorResId, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(ContextCompat.getColor(activity, colorResId));
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
