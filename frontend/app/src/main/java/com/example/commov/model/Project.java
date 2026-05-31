package com.example.commov.model;

import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;

public final class Project {
    @StringRes
    public final int nameResId;
    @StringRes
    public final int descriptionResId;
    public final String initials;
    public final int taskCount;
    public final int progress;
    @ColorRes
    public final int accentColorResId;
    @ColorRes
    public final int badgeColorResId;

    public Project(
            @StringRes int nameResId,
            @StringRes int descriptionResId,
            String initials,
            int taskCount,
            int progress,
            @ColorRes int accentColorResId,
            @ColorRes int badgeColorResId
    ) {
        this.nameResId = nameResId;
        this.descriptionResId = descriptionResId;
        this.initials = initials;
        this.taskCount = taskCount;
        this.progress = progress;
        this.accentColorResId = accentColorResId;
        this.badgeColorResId = badgeColorResId;
    }
}
