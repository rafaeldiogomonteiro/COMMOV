package postgres

import (
	"context"

	"commov/backend/internal/entity"
	"gorm.io/gorm"
)

type TaskTimeEntryRepo struct {
	DB *gorm.DB
}

func (r *TaskTimeEntryRepo) Create(ctx context.Context, entry *entity.TaskTimeEntry) error {
	return r.DB.WithContext(ctx).Create(entry).Error
}

func (r *TaskTimeEntryRepo) ListByTaskID(ctx context.Context, taskID int) ([]entity.TaskTimeEntryView, error) {
	var entries []entity.TaskTimeEntryView
	err := r.DB.WithContext(ctx).
		Table("task_time_entries AS e").
		Select(`
			e.entry_id,
			e.task_id,
			e.user_id,
			u.name AS user_name,
			COALESCE(u.photo, '') AS user_photo,
			e.time_spent,
			e.work_date,
			COALESCE(e.observation, '') AS observation,
			COALESCE(e.photo, '') AS photo,
			e.created_at
		`).
		Joins("INNER JOIN users u ON u.user_id = e.user_id").
		Where("e.task_id = ?", taskID).
		Order("e.created_at DESC").
		Scan(&entries).Error

	return entries, err
}

func (r *TaskTimeEntryRepo) ListByUserID(ctx context.Context, userID int) ([]entity.TaskTimeEntry, error) {
	var entries []entity.TaskTimeEntry
	err := r.DB.WithContext(ctx).
		Where("user_id = ?", userID).
		Order("work_date DESC, created_at DESC").
		Find(&entries).Error

	return entries, err
}
