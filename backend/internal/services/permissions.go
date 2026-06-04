package services

import (
	"context"
	"errors"
	"fmt"

	"commov/backend/internal/entity"
	"commov/backend/internal/postgres"
	"gorm.io/gorm"
)

func authenticatedActor(ctx context.Context, userRepo *postgres.UserRepo, actorUserID int) (*entity.User, error) {
	if actorUserID <= 0 {
		return nil, fmt.Errorf("%w: missing authenticated user", ErrUnauthorized)
	}

	actor, err := userRepo.GetByID(ctx, actorUserID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, fmt.Errorf("%w: authenticated user not found", ErrUnauthorized)
		}
		return nil, fmt.Errorf("get authenticated user: %w", err)
	}
	if !actor.Active {
		return nil, fmt.Errorf("%w: authenticated user is inactive", ErrUnauthorized)
	}

	return actor, nil
}

func actorWithManagementRole(ctx context.Context, userRepo *postgres.UserRepo, actorUserID int) (*entity.User, error) {
	actor, err := authenticatedActor(ctx, userRepo, actorUserID)
	if err != nil {
		return nil, err
	}
	if !hasManagementRole(actor) {
		return nil, fmt.Errorf("%w: only admins and project managers can manage projects and tasks", ErrForbidden)
	}

	return actor, nil
}

func hasManagementRole(user *entity.User) bool {
	if user == nil {
		return false
	}

	return user.Role == entity.UserRoleAdmin || user.Role == entity.UserRoleProjectManager
}
