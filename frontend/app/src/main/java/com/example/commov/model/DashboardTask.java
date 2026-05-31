package com.example.commov.model;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

public final class DashboardTask {
    @StringRes
    public final int titleResId;
    @StringRes
    public final int metaResId;
    @StringRes
    public final int statusResId;
    @DrawableRes
    public final int iconResId;
    @ColorRes
    public final int accentColorResId;
    @ColorRes
    public final int iconBackgroundColorResId;
    @ColorRes
    public final int statusBackgroundColorResId;
    @ColorRes
    public final int statusTextColorResId;

    public DashboardTask(
            @StringRes int titleResId,
            @StringRes int metaResId,
            @StringRes int statusResId,
            @DrawableRes int iconResId,
            @ColorRes int accentColorResId,
            @ColorRes int iconBackgroundColorResId,
            @ColorRes int statusBackgroundColorResId,
            @ColorRes int statusTextColorResId
    ) {
        this.titleResId = titleResId;
        this.metaResId = metaResId;
        this.statusResId = statusResId;
        this.iconResId = iconResId;
        this.accentColorResId = accentColorResId;
        this.iconBackgroundColorResId = iconBackgroundColorResId;
        this.statusBackgroundColorResId = statusBackgroundColorResId;
        this.statusTextColorResId = statusTextColorResId;
    }
}
