package postgres

import (
	"context"

	"commov/backend/internal/entity"
	"gorm.io/gorm"
)

type UserRepo struct {
	DB *gorm.DB
}

func (r *UserRepo) Create(ctx context.Context, user *entity.User) error {
	return r.DB.WithContext(ctx).Create(user).Error
}

func (r *UserRepo) GetByID(ctx context.Context, userID int) (*entity.User, error) {
	var user entity.User
	if err := r.DB.WithContext(ctx).First(&user, "user_id = ?", userID).Error; err != nil {
		return nil, err
	}

	return &user, nil
}

func (r *UserRepo) List(ctx context.Context) ([]entity.User, error) {
	var users []entity.User
	err := r.DB.WithContext(ctx).Order("user_id desc").Find(&users).Error
	return users, err
}

func (r *UserRepo) Update(ctx context.Context, user *entity.User) error {
	return r.DB.WithContext(ctx).Save(user).Error
}

func (r *UserRepo) Delete(ctx context.Context, userID int) error {
	return r.DB.WithContext(ctx).Delete(&entity.User{}, "user_id = ?", userID).Error
}
