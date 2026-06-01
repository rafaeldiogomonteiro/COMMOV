package com.example.commov.model;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;

import java.util.List;

public final class Project {
    public final int projectId;
    @StringRes
    public final int nameResId;
    @StringRes
    public final int descriptionResId;
    public final String initials;
    public final int taskCount;
    public final int progress;
    public final List<ProjectMember> members;
    public final List<ProjectTask> tasks;
    @ColorRes
    public final int accentColorResId;
    @ColorRes
    public final int badgeColorResId;

    public Project(
            int projectId,
            @StringRes int nameResId,
            @StringRes int descriptionResId,
            String initials,
            int taskCount,
            int progress,
            List<ProjectMember> members,
            List<ProjectTask> tasks,
            @ColorRes int accentColorResId,
            @ColorRes int badgeColorResId
    ) {
        this.projectId = projectId;
        this.nameResId = nameResId;
        this.descriptionResId = descriptionResId;
        this.initials = initials;
        this.taskCount = taskCount;
        this.progress = progress;
        this.members = members;
        this.tasks = tasks;
        this.accentColorResId = accentColorResId;
        this.badgeColorResId = badgeColorResId;
    }
}
