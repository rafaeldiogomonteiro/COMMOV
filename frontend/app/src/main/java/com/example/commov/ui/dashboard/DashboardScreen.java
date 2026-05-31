package com.example.commov.ui.dashboard;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.commov.R;
import com.example.commov.model.DashboardTask;
import com.example.commov.viewmodel.DashboardUiState;
import com.example.commov.viewmodel.DashboardViewModel;

public final class DashboardScreen {
    private final Activity activity;
    private final DashboardViewModel viewModel = new DashboardViewModel();

    public DashboardScreen(Activity activity) {
        this.activity = activity;
    }

    public void bind() {
        render(viewModel.getState());
    }

    private void render(DashboardUiState state) {
        ((TextView) activity.findViewById(R.id.greetingText))
                .setText(activity.getString(R.string.dashboard_greeting, state.userName));
        ((TextView) activity.findViewById(R.id.pendingCountText)).setText(String.valueOf(state.pendingTasks));
        ((TextView) activity.findViewById(R.id.completedCountText)).setText(String.valueOf(state.completedTasks));
        ((ProgressBar) activity.findViewById(R.id.pendingProgress)).setProgress(state.pendingProgress);
        ((ProgressBar) activity.findViewById(R.id.completedProgress)).setProgress(state.completedProgress);

        LinearLayout tasksFeed = activity.findViewById(R.id.tasksFeed);
        tasksFeed.removeAllViews();

        for (DashboardTask task : state.tasks) {
            tasksFeed.addView(createTaskView(tasksFeed, task));
        }
    }

    private View createTaskView(LinearLayout parent, DashboardTask task) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_dashboard_task, parent, false);

        View accent = view.findViewById(R.id.taskAccent);
        FrameLayout iconContainer = view.findViewById(R.id.taskIconContainer);
        ImageView icon = view.findViewById(R.id.taskIcon);
        TextView title = view.findViewById(R.id.taskTitle);
        TextView meta = view.findViewById(R.id.taskMeta);
        TextView status = view.findViewById(R.id.taskStatus);

        accent.setBackgroundColor(ContextCompat.getColor(activity, task.accentColorResId));
        iconContainer.setBackground(makeRoundBackground(task.iconBackgroundColorResId, 15));
        icon.setImageResource(task.iconResId);
        title.setText(task.titleResId);
        meta.setText(task.metaResId);
        status.setText(task.statusResId);
        status.setAllCaps(true);
        status.setTextColor(ContextCompat.getColor(activity, task.statusTextColorResId));
        status.setBackground(makeRoundBackground(task.statusBackgroundColorResId, 14));
        applyResponsiveTaskWidth(view);

        return view;
    }

    private void applyResponsiveTaskWidth(View view) {
        int screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
        int width = Math.round(screenWidth * 0.88f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, dp(66));
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

    private int dp(int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
