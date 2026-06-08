package entity

import "strings"

const (
	ProjectStatusActive    = "active"
	ProjectStatusCompleted = "completed"
	ProjectStatusOnHold    = "on_hold"

	TaskStatusPending    = "pending"
	TaskStatusInProgress = "in_progress"
	TaskStatusCompleted  = "completed"
	TaskStatusBlocked    = "blocked"
)

func IsValidProjectStatus(status string) bool {
	switch strings.ToLower(strings.TrimSpace(status)) {
	case ProjectStatusActive, ProjectStatusCompleted, ProjectStatusOnHold:
		return true
	default:
		return false
	}
}

func NormalizeProjectStatus(status string) (string, bool) {
	status = strings.ToLower(strings.TrimSpace(status))
	if status == "" {
		status = ProjectStatusActive
	}
	if !IsValidProjectStatus(status) {
		return "", false
	}
	return status, true
}

func IsValidTaskStatus(status string) bool {
	switch strings.ToLower(strings.TrimSpace(status)) {
	case TaskStatusPending, TaskStatusInProgress, TaskStatusCompleted, TaskStatusBlocked:
		return true
	default:
		return false
	}
}

func NormalizeTaskStatus(status string) (string, bool) {
	status = strings.ToLower(strings.TrimSpace(status))
	if status == "" {
		status = TaskStatusPending
	}
	if !IsValidTaskStatus(status) {
		return "", false
	}
	return status, true
}

func IsCompletedStatus(status string) bool {
	return strings.EqualFold(strings.TrimSpace(status), TaskStatusCompleted)
}
