package services

import (
	"context"
	"errors"
	"strings"

	"commov/backend/internal/entity"
	"commov/backend/internal/postgres"
)

type UserService struct {
	UserRepo *postgres.UserRepo
}

type CreateUserInput struct {
	Name     string `json:"name"`
	Username string `json:"username"`
	Email    string `json:"email"`
	Password string `json:"password"`
	Photo    string `json:"photo"`
	Role     string `json:"role"`
	Active   *bool  `json:"active"`
}

func (s *UserService) Create(ctx context.Context, input CreateUserInput) (*entity.User, error) {
	if strings.TrimSpace(input.Name) == "" {
		return nil, errors.New("name is required")
	}
	if strings.TrimSpace(input.Username) == "" {
		return nil, errors.New("username is required")
	}
	if strings.TrimSpace(input.Email) == "" {
		return nil, errors.New("email is required")
	}
	if input.Password == "" {
		return nil, errors.New("password is required")
	}

	active := true
	if input.Active != nil {
		active = *input.Active
	}

	user := &entity.User{
		Name:     strings.TrimSpace(input.Name),
		Username: strings.TrimSpace(input.Username),
		Email:    strings.TrimSpace(input.Email),
		Password: input.Password,
		Photo:    strings.TrimSpace(input.Photo),
		Role:     strings.TrimSpace(input.Role),
		Active:   active,
	}

	if user.Role == "" {
		user.Role = "user"
	}

	if err := s.UserRepo.Create(ctx, user); err != nil {
		return nil, err
	}

	return user, nil
}
