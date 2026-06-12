package entity

import "strings"

const (
	ProjectStatusActive    = "active"
	ProjectStatusCompleted = "completed"
	ProjectStatusOnHold    = "on_hold"
	ProjectStatusCancelled = "cancelled"

	TaskStatusTodo      = "todo"
	TaskStatusCompleted = "completed"
)

func IsValidProjectStatus(status string) bool {
	switch strings.ToLower(strings.TrimSpace(status)) {
	case ProjectStatusActive, ProjectStatusCompleted, ProjectStatusOnHold, ProjectStatusCancelled:
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
	case TaskStatusTodo, TaskStatusCompleted, "pending", "in_progress", "blocked":
		return true
	default:
		return false
	}
}

func NormalizeTaskStatus(status string) (string, bool) {
	status = strings.ToLower(strings.TrimSpace(status))
	if status == "" {
		status = TaskStatusTodo
	}
	if !IsValidTaskStatus(status) {
		return "", false
	}
	if status != TaskStatusCompleted {
		status = TaskStatusTodo
	}
	return status, true
}

func IsCompletedStatus(status string) bool {
	return strings.EqualFold(strings.TrimSpace(status), TaskStatusCompleted)
}
