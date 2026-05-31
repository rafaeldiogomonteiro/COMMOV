package com.example.commov.viewmodel;

import com.example.commov.R;
import com.example.commov.model.DashboardTask;

import java.util.Arrays;

public final class DashboardViewModel {
    public DashboardUiState getState() {
        return new DashboardUiState(
                "Ricardo",
                12,
                48,
                65,
                100,
                Arrays.asList(
                        new DashboardTask(
                                R.string.task_code_review_title,
                                R.string.task_code_review_meta,
                                R.string.task_status_expired,
                                R.drawable.ic_alert_triangle,
                                R.color.task_red,
                                R.color.task_red_soft,
                                R.color.task_red_soft,
                                R.color.task_red
                        ),
                        new DashboardTask(
                                R.string.task_api_docs_title,
                                R.string.task_api_docs_meta,
                                R.string.task_status_soon,
                                R.drawable.ic_clock,
                                R.color.task_orange,
                                R.color.task_orange_soft,
                                R.color.task_orange_soft,
                                R.color.task_orange
                        ),
                        new DashboardTask(
                                R.string.task_alignment_title,
                                R.string.task_alignment_meta,
                                R.string.task_status_pending,
                                R.drawable.ic_document,
                                R.color.task_blue,
                                R.color.task_blue_soft,
                                R.color.task_status_gray_bg,
                                R.color.task_status_gray_text
                        ),
                        new DashboardTask(
                                R.string.task_design_refactor_title,
                                R.string.task_design_refactor_meta,
                                R.string.task_status_pending,
                                R.drawable.ic_design,
                                R.color.task_blue,
                                R.color.task_blue_soft,
                                R.color.task_status_gray_bg,
                                R.color.task_status_gray_text
                        )
                )
        );
    }
}
