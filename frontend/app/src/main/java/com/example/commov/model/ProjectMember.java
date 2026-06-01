package com.example.commov.model;

import androidx.annotation.ColorRes;

public final class ProjectMember {
    public final String name;
    public final String initials;
    @ColorRes
    public final int avatarColorResId;

    public ProjectMember(String name, String initials, @ColorRes int avatarColorResId) {
        this.name = name;
        this.initials = initials;
        this.avatarColorResId = avatarColorResId;
    }
}
