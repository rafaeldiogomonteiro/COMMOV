package services

import (
	"context"
	"errors"
	"fmt"
	"strings"

	"commov/backend/internal/entity"
	"commov/backend/internal/postgres"
	"golang.org/x/crypto/bcrypt"
	"gorm.io/gorm"
)

var (
	ErrUnauthorized = errors.New("unauthorized")
	ErrForbidden    = errors.New("forbidden")
	ErrValidation   = errors.New("validation error")
	ErrConflict     = errors.New("conflict")
	ErrNotFound     = errors.New("not found")
)

type UserService struct {
	UserRepo    *postgres.UserRepo
	ProjectRepo *postgres.ProjectRepo
	AuthService *AuthService
}

func (s *UserService) EnsureDefaultUser(ctx context.Context, defaultUser string, password string) (*entity.User, bool, error) {
	defaultUser = strings.ToLower(strings.TrimSpace(defaultUser))
	if defaultUser == "" && strings.TrimSpace(password) == "" {
		return nil, false, nil
	}
	if defaultUser == "" || strings.TrimSpace(password) == "" {
		return nil, false, validationError("default user and password are required")
	}
	if len(defaultUser) > 160 {
		return nil, false, validationError("default user must be 160 characters or fewer")
	}

	user, err := s.UserRepo.GetByEmail(ctx, defaultUser)
	if err == nil {
		if user.Role != entity.UserRoleAdmin || !user.Active {
			user.Role = entity.UserRoleAdmin
			user.Active = true
			if err := s.UserRepo.Update(ctx, user); err != nil {
				return nil, false, fmt.Errorf("update default user: %w", err)
			}
		}
		return user, false, nil
	}
	if !errors.Is(err, gorm.ErrRecordNotFound) {
		return nil, false, fmt.Errorf("get default user: %w", err)
	}

	username := defaultUsername(defaultUser)
	if username == "" {
		return nil, false, validationError("default username is required")
	}
	if len(username) > 80 {
		return nil, false, validationError("default username must be 80 characters or fewer")
	}

	usernameExists, err := s.UserRepo.ExistsByUsername(ctx, username)
	if err != nil {
		return nil, false, fmt.Errorf("check default username: %w", err)
	}
	if usernameExists {
		return nil, false, conflictError("default username already exists")
	}

	passwordHash, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	if err != nil {
		return nil, false, fmt.Errorf("hash default password: %w", err)
	}

	user = &entity.User{
		Name:     "Default Admin",
		Username: username,
		Email:    defaultUser,
		Password: string(passwordHash),
		Role:     entity.UserRoleAdmin,
		Active:   true,
	}
	if err := s.UserRepo.Create(ctx, user); err != nil {
		if existingUser, getErr := s.UserRepo.GetByEmail(ctx, defaultUser); getErr == nil {
			if existingUser.Role != entity.UserRoleAdmin || !existingUser.Active {
				existingUser.Role = entity.UserRoleAdmin
				existingUser.Active = true
				if updateErr := s.UserRepo.Update(ctx, existingUser); updateErr != nil {
					return nil, false, fmt.Errorf("update default user: %w", updateErr)
				}
			}
			return existingUser, false, nil
		}
		return nil, false, fmt.Errorf("create default user: %w", err)
	}

	return user, true, nil
}

func (s *UserService) Create(
	ctx context.Context,
	actorUserID int,
	name string,
	username string,
	email string,
	password string,
	photo string,
	roleValue string,
	activeValue *bool,
) (*entity.User, error) {
	if err := s.ensureAdmin(ctx, actorUserID); err != nil {
		return nil, err
	}

	name = strings.TrimSpace(name)
	username = strings.TrimSpace(username)
	email = strings.ToLower(strings.TrimSpace(email))
	photo = strings.TrimSpace(photo)
	role := entity.UserRole(strings.TrimSpace(roleValue))

	if err := validateUserIdentity(name, username, email); err != nil {
		return nil, err
	}
	if password == "" {
		return nil, validationError("password is required")
	}

	passwordHash, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	if err != nil {
		return nil, fmt.Errorf("hash password: %w", err)
	}

	active := true
	if activeValue != nil {
		active = *activeValue
	}

	user := &entity.User{
		Name:     name,
		Username: username,
		Email:    email,
		Password: string(passwordHash),
		Photo:    photo,
		Role:     role,
		Active:   active,
	}

	if user.Role == "" {
		user.Role = entity.UserRoleUser
	}
	if !entity.IsValidUserRole(user.Role) {
		return nil, validationError("invalid user role")
	}

	usernameExists, err := s.UserRepo.ExistsByUsername(ctx, user.Username)
	if err != nil {
		return nil, fmt.Errorf("check username: %w", err)
	}
	if usernameExists {
		return nil, conflictError("username already exists")
	}

	emailExists, err := s.UserRepo.ExistsByEmail(ctx, user.Email)
	if err != nil {
		return nil, fmt.Errorf("check email: %w", err)
	}
	if emailExists {
		return nil, conflictError("email already exists")
	}

	if err := s.UserRepo.Create(ctx, user); err != nil {
		return nil, fmt.Errorf("create user: %w", err)
	}

	return user, nil
}

// RegisterPublic creates a new user account without requiring an authenticated admin.
// Intended for self-registration from the login screen. Role is forced to "user".
func (s *UserService) RegisterPublic(ctx context.Context, name string, username string, email string, password string) (*entity.User, error) {
	name = strings.TrimSpace(name)
	username = strings.TrimSpace(username)
	email = strings.ToLower(strings.TrimSpace(email))

	if err := validateUserIdentity(name, username, email); err != nil {
		return nil, err
	}
	if password == "" {
		return nil, validationError("password is required")
	}
	if len(password) < 6 {
		return nil, validationError("password must have at least 6 characters")
	}

	passwordHash, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
	if err != nil {
		return nil, fmt.Errorf("hash password: %w", err)
	}

	user := &entity.User{
		Name:     name,
		Username: username,
		Email:    email,
		Password: string(passwordHash),
		Photo:    "",
		Role:     entity.UserRoleUser,
		Active:   true,
	}

	usernameExists, err := s.UserRepo.ExistsByUsername(ctx, user.Username)
	if err != nil {
		return nil, fmt.Errorf("check username: %w", err)
	}
	if usernameExists {
		return nil, conflictError("username already exists")
	}

	emailExists, err := s.UserRepo.ExistsByEmail(ctx, user.Email)
	if err != nil {
		return nil, fmt.Errorf("check email: %w", err)
	}
	if emailExists {
		return nil, conflictError("email already exists")
	}

	if err := s.UserRepo.Create(ctx, user); err != nil {
		return nil, fmt.Errorf("create user: %w", err)
	}

	return user, nil
}

func (s *UserService) List(ctx context.Context, actorUserID int) ([]entity.User, error) {
	if err := s.ensureAdmin(ctx, actorUserID); err != nil {
		return nil, err
	}

	users, err := s.UserRepo.List(ctx)
	if err != nil {
		return nil, fmt.Errorf("list users: %w", err)
	}

	return users, nil
}

func (s *UserService) Get(ctx context.Context, actorUserID int, userID int) (*entity.User, error) {
	if err := s.ensureAdmin(ctx, actorUserID); err != nil {
		return nil, err
	}
	if userID <= 0 {
		return nil, validationError("userId is invalid")
	}

	user, err := s.UserRepo.GetByID(ctx, userID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, notFoundError("user not found")
		}
		return nil, fmt.Errorf("get user: %w", err)
	}

	return user, nil
}

type UserUpdateInput struct {
	Name     *string
	Username *string
	Email    *string
	Password *string
	Photo    *string
	Role     *string
	Active   *bool
}

func (s *UserService) Update(ctx context.Context, actorUserID int, userID int, input UserUpdateInput) (*entity.User, error) {
	if err := s.ensureAdmin(ctx, actorUserID); err != nil {
		return nil, err
	}
	if userID <= 0 {
		return nil, validationError("userId is invalid")
	}

	user, err := s.UserRepo.GetByID(ctx, userID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, notFoundError("user not found")
		}
		return nil, fmt.Errorf("get user: %w", err)
	}

	passwordChanged := false
	deactivated := false

	if input.Name != nil {
		name := strings.TrimSpace(*input.Name)
		if name == "" {
			return nil, validationError("name is required")
		}
		if len(name) > 120 {
			return nil, validationError("name must be 120 characters or fewer")
		}
		user.Name = name
	}
	if input.Username != nil {
		username := strings.TrimSpace(*input.Username)
		if username == "" {
			return nil, validationError("username is required")
		}
		if len(username) > 80 {
			return nil, validationError("username must be 80 characters or fewer")
		}
		exists, err := s.UserRepo.ExistsByUsername(ctx, username)
		if err != nil {
			return nil, fmt.Errorf("check username: %w", err)
		}
		if exists && !strings.EqualFold(user.Username, username) {
			return nil, conflictError("username already exists")
		}
		user.Username = username
	}
	if input.Email != nil {
		email := strings.ToLower(strings.TrimSpace(*input.Email))
		if email == "" {
			return nil, validationError("email is required")
		}
		if len(email) > 160 {
			return nil, validationError("email must be 160 characters or fewer")
		}
		exists, err := s.UserRepo.ExistsByEmail(ctx, email)
		if err != nil {
			return nil, fmt.Errorf("check email: %w", err)
		}
		if exists && !strings.EqualFold(user.Email, email) {
			return nil, conflictError("email already exists")
		}
		user.Email = email
	}
	if input.Password != nil {
		password := strings.TrimSpace(*input.Password)
		if password == "" {
			return nil, validationError("password cannot be empty")
		}
		passwordHash, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
		if err != nil {
			return nil, fmt.Errorf("hash password: %w", err)
		}
		user.Password = string(passwordHash)
		passwordChanged = true
	}
	if input.Photo != nil {
		user.Photo = strings.TrimSpace(*input.Photo)
	}
	if input.Role != nil {
		role := entity.UserRole(strings.TrimSpace(*input.Role))
		if !entity.IsValidUserRole(role) {
			return nil, validationError("invalid user role")
		}
		user.Role = role
	}
	if input.Active != nil {
		if user.Active && !*input.Active {
			deactivated = true
		}
		user.Active = *input.Active
	}

	if err := s.UserRepo.Update(ctx, user); err != nil {
		return nil, fmt.Errorf("update user: %w", err)
	}

	if s.AuthService != nil && (passwordChanged || deactivated) {
		s.AuthService.RevokeUserTokens(user.UserID)
	}

	return user, nil
}

func (s *UserService) Delete(ctx context.Context, actorUserID int, userID int) error {
	if err := s.ensureAdmin(ctx, actorUserID); err != nil {
		return err
	}
	if userID <= 0 {
		return validationError("userId is invalid")
	}
	if actorUserID == userID {
		return validationError("cannot delete your own user")
	}

	user, err := s.UserRepo.GetByID(ctx, userID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return notFoundError("user not found")
		}
		return fmt.Errorf("get user: %w", err)
	}

	if user.Role == entity.UserRoleAdmin && user.Active {
		remainingAdmins, err := s.UserRepo.CountActiveAdmins(ctx, userID)
		if err != nil {
			return fmt.Errorf("count active admins: %w", err)
		}
		if remainingAdmins == 0 {
			return validationError("cannot delete the last active admin")
		}
	}

	if s.ProjectRepo != nil {
		references, err := s.ProjectRepo.CountUserReferences(ctx, userID)
		if err != nil {
			return fmt.Errorf("count project references: %w", err)
		}
		if references > 0 {
			return validationError("user is referenced by one or more projects")
		}
	}

	if err := s.UserRepo.Delete(ctx, userID); err != nil {
		return fmt.Errorf("delete user: %w", err)
	}

	if s.AuthService != nil {
		s.AuthService.RevokeUserTokens(userID)
	}

	return nil
}

func validateUserIdentity(name string, username string, email string) error {
	if name == "" {
		return validationError("name is required")
	}
	if len(name) > 120 {
		return validationError("name must be 120 characters or fewer")
	}
	if username == "" {
		return validationError("username is required")
	}
	if len(username) > 80 {
		return validationError("username must be 80 characters or fewer")
	}
	if email == "" {
		return validationError("email is required")
	}
	if len(email) > 160 {
		return validationError("email must be 160 characters or fewer")
	}

	return nil
}

func (s *UserService) ensureAdmin(ctx context.Context, actorUserID int) error {
	if actorUserID <= 0 {
		return fmt.Errorf("%w: missing authenticated user", ErrUnauthorized)
	}

	actor, err := s.UserRepo.GetByID(ctx, actorUserID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return fmt.Errorf("%w: authenticated user not found", ErrUnauthorized)
		}
		return fmt.Errorf("get authenticated user: %w", err)
	}
	if actor.Role != entity.UserRoleAdmin {
		return fmt.Errorf("%w: only admins can manage users", ErrForbidden)
	}

	return nil
}

func defaultUsername(defaultUser string) string {
	username := defaultUser
	if at := strings.Index(defaultUser, "@"); at > 0 {
		username = defaultUser[:at]
	}

	return strings.TrimSpace(username)
}

func validationError(message string) error {
	return fmt.Errorf("%w: %s", ErrValidation, message)
}

func conflictError(message string) error {
	return fmt.Errorf("%w: %s", ErrConflict, message)
}

func notFoundError(message string) error {
	return fmt.Errorf("%w: %s", ErrNotFound, message)
}
