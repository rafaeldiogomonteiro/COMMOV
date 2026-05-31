package com.example.commov.viewmodel;

import com.example.commov.model.DashboardTask;

import java.util.List;

public final class DashboardUiState {
    public final String userName;
    public final int pendingTasks;
    public final int completedTasks;
    public final int pendingProgress;
    public final int completedProgress;
    public final List<DashboardTask> tasks;

    public DashboardUiState(
            String userName,
            int pendingTasks,
            int completedTasks,
            int pendingProgress,
            int completedProgress,
            List<DashboardTask> tasks
    ) {
        this.userName = userName;
        this.pendingTasks = pendingTasks;
        this.completedTasks = completedTasks;
        this.pendingProgress = pendingProgress;
        this.completedProgress = completedProgress;
        this.tasks = tasks;
    }
}
