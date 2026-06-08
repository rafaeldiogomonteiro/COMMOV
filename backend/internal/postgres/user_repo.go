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

func (r *UserRepo) ExistsByUsername(ctx context.Context, username string) (bool, error) {
	var count int64
	err := r.DB.WithContext(ctx).
		Model(&entity.User{}).
		Where("username = ?", username).
		Count(&count).Error

	return count > 0, err
}

func (r *UserRepo) ExistsByEmail(ctx context.Context, email string) (bool, error) {
	var count int64
	err := r.DB.WithContext(ctx).
		Model(&entity.User{}).
		Where("email = ?", email).
		Count(&count).Error

	return count > 0, err
}

func (r *UserRepo) GetByID(ctx context.Context, userID int) (*entity.User, error) {
	var user entity.User
	if err := r.DB.WithContext(ctx).First(&user, "user_id = ?", userID).Error; err != nil {
		return nil, err
	}

	return &user, nil
}

func (r *UserRepo) GetByEmail(ctx context.Context, email string) (*entity.User, error) {
	var user entity.User
	if err := r.DB.WithContext(ctx).First(&user, "email = ?", email).Error; err != nil {
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

func (r *UserRepo) CountActiveAdmins(ctx context.Context, excludeUserID int) (int64, error) {
	query := r.DB.WithContext(ctx).
		Model(&entity.User{}).
		Where("role = ? AND active = ?", entity.UserRoleAdmin, true)
	if excludeUserID > 0 {
		query = query.Where("user_id <> ?", excludeUserID)
	}

	var count int64
	err := query.Count(&count).Error
	return count, err
}
