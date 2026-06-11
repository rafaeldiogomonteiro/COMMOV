package postgres

import (
	"context"

	"commov/backend/internal/entity"
	"gorm.io/gorm"
)

type TaskUserRepo struct {
	DB *gorm.DB
}

func (r *TaskUserRepo) Create(ctx context.Context, taskUser *entity.TaskUser) error {
	return r.DB.WithContext(ctx).Create(taskUser).Error
}

func (r *TaskUserRepo) ListByTaskID(ctx context.Context, taskID int) ([]entity.TaskUser, error) {
	var taskUsers []entity.TaskUser
	err := r.DB.WithContext(ctx).Where("task_id = ?", taskID).Find(&taskUsers).Error
	return taskUsers, err
}

func (r *TaskUserRepo) ListUserIDsByTaskID(ctx context.Context, taskID int) ([]int, error) {
	var userIDs []int
	err := r.DB.WithContext(ctx).
		Model(&entity.TaskUser{}).
		Where("task_id = ?", taskID).
		Pluck("user_id", &userIDs).Error

	return userIDs, err
}

func (r *TaskUserRepo) ExistsByTaskAndUser(ctx context.Context, taskID int, userID int) (bool, error) {
	var count int64
	err := r.DB.WithContext(ctx).
		Model(&entity.TaskUser{}).
		Where("task_id = ? AND user_id = ?", taskID, userID).
		Count(&count).Error

	return count > 0, err
}

func (r *TaskUserRepo) CountByTaskID(ctx context.Context, taskID int) (int64, error) {
	var count int64
	err := r.DB.WithContext(ctx).
		Model(&entity.TaskUser{}).
		Where("task_id = ?", taskID).
		Count(&count).Error

	return count, err
}

func (r *TaskUserRepo) DeleteByTaskAndUser(ctx context.Context, taskID int, userID int) error {
	return r.DB.WithContext(ctx).
		Delete(&entity.TaskUser{}, "task_id = ? AND user_id = ?", taskID, userID).Error
}

func (r *TaskUserRepo) ReplaceForTask(ctx context.Context, taskID int, userIDs []int) error {
	return r.DB.WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		if err := tx.Delete(&entity.TaskUser{}, "task_id = ?", taskID).Error; err != nil {
			return err
		}
		for _, userID := range userIDs {
			if err := tx.Create(&entity.TaskUser{
				TaskID: taskID,
				UserID: userID,
			}).Error; err != nil {
				return err
			}
		}

		return nil
	})
}
