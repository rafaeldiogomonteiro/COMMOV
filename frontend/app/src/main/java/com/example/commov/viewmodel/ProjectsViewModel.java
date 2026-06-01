package com.example.commov.viewmodel;

import com.example.commov.R;
import com.example.commov.model.DashboardTask;
import com.example.commov.model.Project;
import com.example.commov.model.ProjectMember;
import com.example.commov.model.ProjectTask;

import java.util.Arrays;
import java.util.Collections;

public final class ProjectsViewModel {
    public ProjectsUiState getState() {
        return new ProjectsUiState(Arrays.asList(
                new Project(
                        1,
                        R.string.project_alpha_name,
                        R.string.project_alpha_description,
                        "AP",
                        12,
                        72,
                        Arrays.asList(
                                member("Ricardo Silva", "RS", R.color.bottom_nav_selected),
                                member("Ana Costa", "AC", R.color.project_green),
                                member("Marta Reis", "MR", R.color.task_orange),
                                member("Joao Lima", "JL", R.color.project_purple)
                        ),
                        Arrays.asList(
                                new ProjectTask(
                                        task(
                                                R.string.task_code_review_title,
                                                R.string.task_code_review_meta,
                                                R.string.task_status_expired,
                                                R.drawable.ic_alert_triangle,
                                                R.color.task_red,
                                                R.color.task_red_soft,
                                                R.color.task_red_soft,
                                                R.color.task_red
                                        ),
                                        Arrays.asList(
                                                member("Ricardo Silva", "RS", R.color.bottom_nav_selected),
                                                member("Ana Costa", "AC", R.color.project_green)
                                        )
                                ),
                                new ProjectTask(
                                        task(
                                                R.string.task_api_docs_title,
                                                R.string.task_api_docs_meta,
                                                R.string.task_status_soon,
                                                R.drawable.ic_clock,
                                                R.color.task_orange,
                                                R.color.task_orange_soft,
                                                R.color.task_orange_soft,
                                                R.color.task_orange
                                        ),
                                        Arrays.asList(
                                                member("Joao Lima", "JL", R.color.project_purple),
                                                member("Ricardo Silva", "RS", R.color.bottom_nav_selected)
                                        )
                                ),
                                new ProjectTask(
                                        task(
                                                R.string.task_alignment_title,
                                                R.string.task_alignment_meta,
                                                R.string.task_status_pending,
                                                R.drawable.ic_document,
                                                R.color.task_blue,
                                                R.color.task_blue_soft,
                                                R.color.task_status_gray_bg,
                                                R.color.task_status_gray_text
                                        ),
                                        Arrays.asList(
                                                member("Marta Reis", "MR", R.color.task_orange),
                                                member("Ana Costa", "AC", R.color.project_green),
                                                member("Joao Lima", "JL", R.color.project_purple)
                                        )
                                )
                        ),
                        R.color.bottom_nav_selected,
                        R.color.task_blue_soft
                ),
                new Project(
                        2,
                        R.string.project_infrastructure_name,
                        R.string.project_infrastructure_description,
                        "IN",
                        8,
                        45,
                        Arrays.asList(
                                member("Bruno Alves", "BA", R.color.project_green),
                                member("Sofia Pinto", "SP", R.color.task_blue),
                                member("Tiago Sousa", "TS", R.color.task_status_gray_text)
                        ),
                        Collections.emptyList(),
                        R.color.project_green,
                        R.color.project_green_soft
                ),
                new Project(
                        3,
                        R.string.project_marketing_name,
                        R.string.project_marketing_description,
                        "MK",
                        6,
                        63,
                        Arrays.asList(
                                member("Marta Reis", "MR", R.color.task_orange),
                                member("Ana Costa", "AC", R.color.bottom_nav_selected),
                                member("Nuno Rocha", "NR", R.color.project_purple)
                        ),
                        Collections.emptyList(),
                        R.color.task_orange,
                        R.color.task_orange_soft
                ),
                new Project(
                        4,
                        R.string.project_product_name,
                        R.string.project_product_description,
                        "PD",
                        10,
                        38,
                        Arrays.asList(
                                member("Joao Lima", "JL", R.color.project_purple),
                                member("Sofia Pinto", "SP", R.color.project_green),
                                member("Ricardo Silva", "RS", R.color.bottom_nav_selected),
                                member("Bruno Alves", "BA", R.color.task_orange),
                                member("Nuno Rocha", "NR", R.color.task_status_gray_text)
                        ),
                        Collections.emptyList(),
                        R.color.project_purple,
                        R.color.project_purple_soft
                )
        ));
    }

    private ProjectMember member(String name, String initials, int avatarColorResId) {
        return new ProjectMember(name, initials, avatarColorResId);
    }

    private DashboardTask task(
            int titleResId,
            int metaResId,
            int statusResId,
            int iconResId,
            int accentColorResId,
            int iconBackgroundColorResId,
            int statusBackgroundColorResId,
            int statusTextColorResId
    ) {
        return new DashboardTask(
                titleResId,
                metaResId,
                statusResId,
                iconResId,
                accentColorResId,
                iconBackgroundColorResId,
                statusBackgroundColorResId,
                statusTextColorResId
        );
    }
}
