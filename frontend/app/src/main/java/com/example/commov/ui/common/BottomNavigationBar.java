package com.example.commov.ui.common;

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.commov.R;
import com.example.commov.ui.dashboard.DashboardActivity;
import com.example.commov.ui.projects.ProjectsActivity;
import com.example.commov.ui.settings.SettingsActivity;

public final class BottomNavigationBar {
    public enum Destination {
        HOME,
        PROJECTS,
        SETTINGS
    }

    private final Activity activity;
    private final Destination selectedDestination;

    private LinearLayout homeItem;
    private LinearLayout projectsItem;
    private LinearLayout settingsItem;
    private ImageView homeIcon;
    private ImageView projectsIcon;
    private ImageView settingsIcon;
    private TextView homeLabel;
    private TextView projectsLabel;
    private TextView settingsLabel;

    public BottomNavigationBar(Activity activity, Destination selectedDestination) {
        this.activity = activity;
        this.selectedDestination = selectedDestination;
    }

    public void bind() {
        homeItem = activity.findViewById(R.id.homeNavItem);
        projectsItem = activity.findViewById(R.id.projectsNavItem);
        settingsItem = activity.findViewById(R.id.settingsNavItem);
        homeIcon = activity.findViewById(R.id.homeNavIcon);
        projectsIcon = activity.findViewById(R.id.projectsNavIcon);
        settingsIcon = activity.findViewById(R.id.settingsNavIcon);
        homeLabel = activity.findViewById(R.id.homeNavLabel);
        projectsLabel = activity.findViewById(R.id.projectsNavLabel);
        settingsLabel = activity.findViewById(R.id.settingsNavLabel);

        renderSelectedDestination();
        bindNavigationEvents();
    }

    private void renderSelectedDestination() {
        renderItem(homeIcon, homeLabel, selectedDestination == Destination.HOME);
        renderItem(projectsIcon, projectsLabel, selectedDestination == Destination.PROJECTS);
        renderItem(settingsIcon, settingsLabel, selectedDestination == Destination.SETTINGS);
    }

    private void renderItem(ImageView icon, TextView label, boolean selected) {
        int color = selected ? R.color.bottom_nav_selected : R.color.bottom_nav_unselected;
        int resolvedColor = ContextCompat.getColor(activity, color);
        icon.setColorFilter(resolvedColor);
        label.setTextColor(resolvedColor);
        label.setTypeface(null, selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
    }

    private void bindNavigationEvents() {
        homeItem.setOnClickListener(view -> navigateTo(Destination.HOME, DashboardActivity.class));
        projectsItem.setOnClickListener(view -> navigateTo(Destination.PROJECTS, ProjectsActivity.class));
        settingsItem.setOnClickListener(view -> navigateTo(Destination.SETTINGS, SettingsActivity.class));
    }

    private void navigateTo(Destination destination, Class<? extends Activity> activityClass) {
        if (selectedDestination == destination) {
            return;
        }

        Intent intent = new Intent(activity, activityClass);
        activity.startActivity(intent);
        activity.finish();
        activity.overridePendingTransition(0, 0);
    }
}
