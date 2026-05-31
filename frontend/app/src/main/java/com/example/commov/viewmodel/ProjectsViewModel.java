package com.example.commov.viewmodel;

import com.example.commov.R;
import com.example.commov.model.Project;

import java.util.Arrays;

public final class ProjectsViewModel {
    public ProjectsUiState getState() {
        return new ProjectsUiState(Arrays.asList(
                new Project(
                        R.string.project_alpha_name,
                        R.string.project_alpha_description,
                        "AP",
                        12,
                        72,
                        R.color.bottom_nav_selected,
                        R.color.task_blue_soft
                ),
                new Project(
                        R.string.project_infrastructure_name,
                        R.string.project_infrastructure_description,
                        "IN",
                        8,
                        45,
                        R.color.project_green,
                        R.color.project_green_soft
                ),
                new Project(
                        R.string.project_marketing_name,
                        R.string.project_marketing_description,
                        "MK",
                        6,
                        63,
                        R.color.task_orange,
                        R.color.task_orange_soft
                ),
                new Project(
                        R.string.project_product_name,
                        R.string.project_product_description,
                        "PD",
                        10,
                        38,
                        R.color.project_purple,
                        R.color.project_purple_soft
                )
        ));
    }
}
