package postgres

import (
	"context"

	"commov/backend/internal/entity"
	"gorm.io/gorm"
)

type ProjectUserRepo struct {
	DB *gorm.DB
}

func (r *ProjectUserRepo) Create(ctx context.Context, projectUser *entity.ProjectUser) error {
	return r.DB.WithContext(ctx).Create(projectUser).Error
}

func (r *ProjectUserRepo) GetByID(ctx context.Context, projectUserID int) (*entity.ProjectUser, error) {
	var projectUser entity.ProjectUser
	if err := r.DB.WithContext(ctx).First(&projectUser, "project_user_id = ?", projectUserID).Error; err != nil {
		return nil, err
	}

	return &projectUser, nil
}

func (r *ProjectUserRepo) List(ctx context.Context) ([]entity.ProjectUser, error) {
	var projectUsers []entity.ProjectUser
	err := r.DB.WithContext(ctx).Order("project_user_id desc").Find(&projectUsers).Error
	return projectUsers, err
}

func (r *ProjectUserRepo) ListByProjectID(ctx context.Context, projectID int) ([]entity.ProjectUser, error) {
	var projectUsers []entity.ProjectUser
	err := r.DB.WithContext(ctx).Where("project_id = ?", projectID).Find(&projectUsers).Error
	return projectUsers, err
}

func (r *ProjectUserRepo) ListByUserID(ctx context.Context, userID int) ([]entity.ProjectUser, error) {
	var projectUsers []entity.ProjectUser
	err := r.DB.WithContext(ctx).Where("user_id = ?", userID).Find(&projectUsers).Error
	return projectUsers, err
}

func (r *ProjectUserRepo) ExistsByProjectAndUser(ctx context.Context, projectID int, userID int) (bool, error) {
	var count int64
	err := r.DB.WithContext(ctx).
		Model(&entity.ProjectUser{}).
		Where("project_id = ? AND user_id = ?", projectID, userID).
		Count(&count).Error

	return count > 0, err
}

func (r *ProjectUserRepo) Update(ctx context.Context, projectUser *entity.ProjectUser) error {
	return r.DB.WithContext(ctx).Save(projectUser).Error
}

func (r *ProjectUserRepo) Delete(ctx context.Context, projectUserID int) error {
	return r.DB.WithContext(ctx).Delete(&entity.ProjectUser{}, "project_user_id = ?", projectUserID).Error
}

func (r *ProjectUserRepo) DeleteByProjectAndUser(ctx context.Context, projectID int, userID int) error {
	return r.DB.WithContext(ctx).
		Delete(&entity.ProjectUser{}, "project_id = ? AND user_id = ?", projectID, userID).Error
}
