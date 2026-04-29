package postgres

import (
	"context"

	"commov/backend/internal/entity"
	"gorm.io/gorm"
)

type ProjectRepo struct {
	DB *gorm.DB
}

func (r *ProjectRepo) Create(ctx context.Context, project *entity.Project) error {
	return r.DB.WithContext(ctx).Create(project).Error
}

func (r *ProjectRepo) GetByID(ctx context.Context, projectID int) (*entity.Project, error) {
	var project entity.Project
	if err := r.DB.WithContext(ctx).First(&project, "project_id = ?", projectID).Error; err != nil {
		return nil, err
	}

	return &project, nil
}

func (r *ProjectRepo) List(ctx context.Context) ([]entity.Project, error) {
	var projects []entity.Project
	err := r.DB.WithContext(ctx).Order("project_id desc").Find(&projects).Error
	return projects, err
}

func (r *ProjectRepo) Update(ctx context.Context, project *entity.Project) error {
	return r.DB.WithContext(ctx).Save(project).Error
}

func (r *ProjectRepo) Delete(ctx context.Context, projectID int) error {
	return r.DB.WithContext(ctx).Delete(&entity.Project{}, "project_id = ?", projectID).Error
}
