package com.example.commov.model;

import java.util.List;

public final class ProjectTask {
    public final DashboardTask task;
    public final List<ProjectMember> assignees;

    public ProjectTask(DashboardTask task, List<ProjectMember> assignees) {
        this.task = task;
        this.assignees = assignees;
    }
}
