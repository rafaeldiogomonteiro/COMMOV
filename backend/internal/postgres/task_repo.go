package postgres

import (
	"context"

	"commov/backend/internal/entity"
	"gorm.io/gorm"
)

type TaskRepo struct {
	DB *gorm.DB
}

func (r *TaskRepo) Create(ctx context.Context, task *entity.Task) error {
	return r.DB.WithContext(ctx).Create(task).Error
}

func (r *TaskRepo) GetByID(ctx context.Context, taskID int) (*entity.Task, error) {
	var task entity.Task
	if err := r.DB.WithContext(ctx).First(&task, "task_id = ?", taskID).Error; err != nil {
		return nil, err
	}

	return &task, nil
}

func (r *TaskRepo) List(ctx context.Context) ([]entity.Task, error) {
	var tasks []entity.Task
	err := r.DB.WithContext(ctx).Order("task_id desc").Find(&tasks).Error
	return tasks, err
}

func (r *TaskRepo) ListByProjectID(ctx context.Context, projectID int) ([]entity.Task, error) {
	var tasks []entity.Task
	err := r.DB.WithContext(ctx).Where("project_id = ?", projectID).Find(&tasks).Error
	return tasks, err
}

func (r *TaskRepo) ListByUserID(ctx context.Context, userID int) ([]entity.Task, error) {
	var tasks []entity.Task
	err := r.DB.WithContext(ctx).Where("user_id = ?", userID).Find(&tasks).Error
	return tasks, err
}

func (r *TaskRepo) Update(ctx context.Context, task *entity.Task) error {
	return r.DB.WithContext(ctx).Save(task).Error
}

func (r *TaskRepo) Delete(ctx context.Context, taskID int) error {
	return r.DB.WithContext(ctx).Delete(&entity.Task{}, "task_id = ?", taskID).Error
}
