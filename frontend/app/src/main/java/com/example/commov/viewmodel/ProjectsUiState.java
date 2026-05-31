package com.example.commov.viewmodel;

import com.example.commov.model.Project;

import java.util.List;

public final class ProjectsUiState {
    public final List<Project> projects;

    public ProjectsUiState(List<Project> projects) {
        this.projects = projects;
    }
}
