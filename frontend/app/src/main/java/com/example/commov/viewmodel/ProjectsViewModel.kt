package com.example.commov.viewmodel

import com.example.commov.R
import com.example.commov.model.DashboardTask
import com.example.commov.model.Project
import com.example.commov.model.ProjectMember
import com.example.commov.model.ProjectTask

class ProjectsViewModel {
    val state: ProjectsUiState
        get() = ProjectsUiState(
            listOf(
                Project(
                    projectId = 1,
                    nameResId = R.string.project_alpha_name,
                    descriptionResId = R.string.project_alpha_description,
                    initials = "AP",
                    taskCount = 12,
                    progress = 72,
                    members = listOf(
                        member("Ricardo Silva", "RS", R.color.bottom_nav_selected),
                        member("Ana Costa", "AC", R.color.project_green),
                        member("Marta Reis", "MR", R.color.task_orange),
                        member("Joao Lima", "JL", R.color.project_purple)
                    ),
                    tasks = listOf(
                        ProjectTask(
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
                            listOf(
                                member("Ricardo Silva", "RS", R.color.bottom_nav_selected),
                                member("Ana Costa", "AC", R.color.project_green)
                            )
                        ),
                        ProjectTask(
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
                            listOf(
                                member("Joao Lima", "JL", R.color.project_purple),
                                member("Ricardo Silva", "RS", R.color.bottom_nav_selected)
                            )
                        ),
                        ProjectTask(
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
                            listOf(
                                member("Marta Reis", "MR", R.color.task_orange),
                                member("Ana Costa", "AC", R.color.project_green),
                                member("Joao Lima", "JL", R.color.project_purple)
                            )
                        )
                    ),
                    accentColorResId = R.color.bottom_nav_selected,
                    badgeColorResId = R.color.task_blue_soft
                ),
                Project(
                    projectId = 2,
                    nameResId = R.string.project_infrastructure_name,
                    descriptionResId = R.string.project_infrastructure_description,
                    initials = "IN",
                    taskCount = 8,
                    progress = 45,
                    members = listOf(
                        member("Bruno Alves", "BA", R.color.project_green),
                        member("Sofia Pinto", "SP", R.color.task_blue),
                        member("Tiago Sousa", "TS", R.color.task_status_gray_text)
                    ),
                    tasks = emptyList(),
                    accentColorResId = R.color.project_green,
                    badgeColorResId = R.color.project_green_soft
                ),
                Project(
                    projectId = 3,
                    nameResId = R.string.project_marketing_name,
                    descriptionResId = R.string.project_marketing_description,
                    initials = "MK",
                    taskCount = 6,
                    progress = 63,
                    members = listOf(
                        member("Marta Reis", "MR", R.color.task_orange),
                        member("Ana Costa", "AC", R.color.bottom_nav_selected),
                        member("Nuno Rocha", "NR", R.color.project_purple)
                    ),
                    tasks = emptyList(),
                    accentColorResId = R.color.task_orange,
                    badgeColorResId = R.color.task_orange_soft
                ),
                Project(
                    projectId = 4,
                    nameResId = R.string.project_product_name,
                    descriptionResId = R.string.project_product_description,
                    initials = "PD",
                    taskCount = 10,
                    progress = 38,
                    members = listOf(
                        member("Joao Lima", "JL", R.color.project_purple),
                        member("Sofia Pinto", "SP", R.color.project_green),
                        member("Ricardo Silva", "RS", R.color.bottom_nav_selected),
                        member("Bruno Alves", "BA", R.color.task_orange),
                        member("Nuno Rocha", "NR", R.color.task_status_gray_text)
                    ),
                    tasks = emptyList(),
                    accentColorResId = R.color.project_purple,
                    badgeColorResId = R.color.project_purple_soft
                )
            )
        )

    private fun member(name: String, initials: String, avatarColorResId: Int): ProjectMember {
        return ProjectMember(name, initials, avatarColorResId)
    }

    private fun task(
        titleResId: Int,
        metaResId: Int,
        statusResId: Int,
        iconResId: Int,
        accentColorResId: Int,
        iconBackgroundColorResId: Int,
        statusBackgroundColorResId: Int,
        statusTextColorResId: Int
    ): DashboardTask {
        return DashboardTask(
            titleResId,
            metaResId,
            statusResId,
            iconResId,
            accentColorResId,
            iconBackgroundColorResId,
            statusBackgroundColorResId,
            statusTextColorResId
        )
    }
}
