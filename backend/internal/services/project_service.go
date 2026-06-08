package services

import (
	"context"
	"errors"
	"fmt"
	"strings"
	"time"

	"commov/backend/internal/entity"
	"commov/backend/internal/postgres"
	"gorm.io/gorm"
)

const defaultProjectStatus = entity.ProjectStatusActive

const completedProjectStatus = entity.ProjectStatusCompleted

type ProjectCreateInput struct {
	Name             string
	Description      string
	ManagerID        int
	StartDate        time.Time
	EstimatedEndDate time.Time
	MemberIDs        []int
}

type ProjectUpdateInput struct {
	Name             *string
	Description      *string
	Status           *string
	ManagerID        *int
	StartDate        *time.Time
	EstimatedEndDate *time.Time
	ActualEndDate    *time.Time
}

type ProjectService struct {
	ProjectRepo     *postgres.ProjectRepo
	ProjectUserRepo *postgres.ProjectUserRepo
	UserRepo        *postgres.UserRepo
}

func (s *ProjectService) List(ctx context.Context, actorUserID int) ([]entity.Project, error) {
	actor, err := authenticatedActor(ctx, s.UserRepo, actorUserID)
	if err != nil {
		return nil, err
	}

	if hasManagementRole(actor) {
		projects, err := s.ProjectRepo.List(ctx)
		if err != nil {
			return nil, fmt.Errorf("list projects: %w", err)
		}

		return projects, nil
	}

	projects, err := s.ProjectRepo.ListByUserID(ctx, actor.UserID)
	if err != nil {
		return nil, fmt.Errorf("list user projects: %w", err)
	}

	return projects, nil
}

func (s *ProjectService) Get(ctx context.Context, actorUserID int, projectID int) (*entity.Project, error) {
	actor, err := authenticatedActor(ctx, s.UserRepo, actorUserID)
	if err != nil {
		return nil, err
	}

	project, err := s.getProject(ctx, projectID)
	if err != nil {
		return nil, err
	}
	if err := s.ensureProjectAccess(ctx, actor, project); err != nil {
		return nil, err
	}

	return project, nil
}

func (s *ProjectService) Create(ctx context.Context, actorUserID int, input ProjectCreateInput) (*entity.Project, error) {
	actor, err := actorWithManagementRole(ctx, s.UserRepo, actorUserID)
	if err != nil {
		return nil, err
	}

	name, description, status, err := normalizeProjectText(input.Name, input.Description, defaultProjectStatus)
	if err != nil {
		return nil, err
	}
	if err := validateProjectDates(input.StartDate, input.EstimatedEndDate, nil); err != nil {
		return nil, err
	}

	managerID := input.ManagerID
	if managerID <= 0 {
		managerID = actor.UserID
	}

	manager, err := s.getActiveUser(ctx, managerID, "managerId")
	if err != nil {
		return nil, err
	}
	if !hasManagementRole(manager) {
		return nil, validationError("manager must be an admin or project manager")
	}

	memberIDs := uniquePositiveIDs(append([]int{actor.UserID, managerID}, input.MemberIDs...))
	for _, userID := range memberIDs {
		if _, err := s.getActiveUser(ctx, userID, "memberId"); err != nil {
			return nil, err
		}
	}

	project := &entity.Project{
		Name:             name,
		Description:      description,
		Status:           status,
		ManagerID:        managerID,
		CreatedBy:        actor.UserID,
		StartDate:        input.StartDate,
		EstimatedEndDate: input.EstimatedEndDate,
	}
	if err := s.ProjectRepo.Create(ctx, project); err != nil {
		return nil, fmt.Errorf("create project: %w", err)
	}

	for _, userID := range memberIDs {
		if err := s.ensureProjectMember(ctx, project.ProjectID, userID); err != nil {
			return nil, err
		}
	}

	return project, nil
}

func (s *ProjectService) Update(ctx context.Context, actorUserID int, projectID int, input ProjectUpdateInput) (*entity.Project, error) {
	if _, err := actorWithManagementRole(ctx, s.UserRepo, actorUserID); err != nil {
		return nil, err
	}

	project, err := s.getProject(ctx, projectID)
	if err != nil {
		return nil, err
	}

	if input.Name != nil {
		name := strings.TrimSpace(*input.Name)
		if name == "" {
			return nil, validationError("name is required")
		}
		if len(name) > 160 {
			return nil, validationError("name must be 160 characters or fewer")
		}
		project.Name = name
	}
	if input.Description != nil {
		project.Description = strings.TrimSpace(*input.Description)
	}
	if input.Status != nil {
		status, ok := entity.NormalizeProjectStatus(*input.Status)
		if !ok {
			return nil, validationError("invalid project status")
		}
		project.Status = status
	}
	if input.ManagerID != nil {
		manager, err := s.getActiveUser(ctx, *input.ManagerID, "managerId")
		if err != nil {
			return nil, err
		}
		if !hasManagementRole(manager) {
			return nil, validationError("manager must be an admin or project manager")
		}
		project.ManagerID = *input.ManagerID
	}
	if input.StartDate != nil {
		project.StartDate = *input.StartDate
	}
	if input.EstimatedEndDate != nil {
		project.EstimatedEndDate = *input.EstimatedEndDate
	}
	if input.ActualEndDate != nil {
		project.ActualEndDate = input.ActualEndDate
	}
	if entity.IsCompletedStatus(project.Status) {
		if project.ActualEndDate == nil {
			today := currentDate()
			project.ActualEndDate = &today
		}
	}

	if err := validateProjectDates(project.StartDate, project.EstimatedEndDate, project.ActualEndDate); err != nil {
		return nil, err
	}

	if err := s.ensureProjectMember(ctx, project.ProjectID, project.ManagerID); err != nil {
		return nil, err
	}
	if err := s.ProjectRepo.Update(ctx, project); err != nil {
		return nil, fmt.Errorf("update project: %w", err)
	}

	return project, nil
}

func (s *ProjectService) Delete(ctx context.Context, actorUserID int, projectID int) error {
	if _, err := actorWithManagementRole(ctx, s.UserRepo, actorUserID); err != nil {
		return err
	}
	if _, err := s.getProject(ctx, projectID); err != nil {
		return err
	}

	if err := s.ProjectRepo.Delete(ctx, projectID); err != nil {
		return fmt.Errorf("delete project: %w", err)
	}

	return nil
}

func (s *ProjectService) ListMembers(ctx context.Context, actorUserID int, projectID int) ([]entity.User, error) {
	actor, err := authenticatedActor(ctx, s.UserRepo, actorUserID)
	if err != nil {
		return nil, err
	}

	project, err := s.getProject(ctx, projectID)
	if err != nil {
		return nil, err
	}
	if err := s.ensureProjectAccess(ctx, actor, project); err != nil {
		return nil, err
	}

	projectUsers, err := s.ProjectUserRepo.ListByProjectID(ctx, projectID)
	if err != nil {
		return nil, fmt.Errorf("list project members: %w", err)
	}

	users := make([]entity.User, 0, len(projectUsers))
	for _, projectUser := range projectUsers {
		user, err := s.UserRepo.GetByID(ctx, projectUser.UserID)
		if err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				continue
			}
			return nil, fmt.Errorf("get project member: %w", err)
		}
		users = append(users, *user)
	}

	return users, nil
}

func (s *ProjectService) AddMember(ctx context.Context, actorUserID int, projectID int, userID int) error {
	if _, err := actorWithManagementRole(ctx, s.UserRepo, actorUserID); err != nil {
		return err
	}
	if _, err := s.getProject(ctx, projectID); err != nil {
		return err
	}
	if _, err := s.getActiveUser(ctx, userID, "userId"); err != nil {
		return err
	}

	return s.ensureProjectMember(ctx, projectID, userID)
}

func (s *ProjectService) RemoveMember(ctx context.Context, actorUserID int, projectID int, userID int) error {
	if _, err := actorWithManagementRole(ctx, s.UserRepo, actorUserID); err != nil {
		return err
	}

	project, err := s.getProject(ctx, projectID)
	if err != nil {
		return err
	}
	if userID <= 0 {
		return validationError("userId is invalid")
	}
	if userID == project.ManagerID {
		return validationError("project manager cannot be removed from the project")
	}

	if err := s.ProjectUserRepo.DeleteByProjectAndUser(ctx, projectID, userID); err != nil {
		return fmt.Errorf("remove project member: %w", err)
	}

	return nil
}

func (s *ProjectService) getProject(ctx context.Context, projectID int) (*entity.Project, error) {
	if projectID <= 0 {
		return nil, validationError("projectId is invalid")
	}

	project, err := s.ProjectRepo.GetByID(ctx, projectID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, notFoundError("project not found")
		}
		return nil, fmt.Errorf("get project: %w", err)
	}

	return project, nil
}

func (s *ProjectService) getActiveUser(ctx context.Context, userID int, fieldName string) (*entity.User, error) {
	if userID <= 0 {
		return nil, validationError(fieldName + " is invalid")
	}

	user, err := s.UserRepo.GetByID(ctx, userID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, notFoundError(fieldName + " not found")
		}
		return nil, fmt.Errorf("get user: %w", err)
	}
	if !user.Active {
		return nil, validationError(fieldName + " is inactive")
	}

	return user, nil
}

func (s *ProjectService) ensureProjectAccess(ctx context.Context, actor *entity.User, project *entity.Project) error {
	if hasManagementRole(actor) {
		return nil
	}

	isMember, err := s.ProjectUserRepo.ExistsByProjectAndUser(ctx, project.ProjectID, actor.UserID)
	if err != nil {
		return fmt.Errorf("check project membership: %w", err)
	}
	if !isMember {
		return fmt.Errorf("%w: project is not assigned to this user", ErrForbidden)
	}

	return nil
}

func (s *ProjectService) ensureProjectMember(ctx context.Context, projectID int, userID int) error {
	exists, err := s.ProjectUserRepo.ExistsByProjectAndUser(ctx, projectID, userID)
	if err != nil {
		return fmt.Errorf("check project member: %w", err)
	}
	if exists {
		return nil
	}

	if err := s.ProjectUserRepo.Create(ctx, &entity.ProjectUser{
		ProjectID: projectID,
		UserID:    userID,
	}); err != nil {
		return fmt.Errorf("add project member: %w", err)
	}

	return nil
}

func normalizeProjectText(name string, description string, status string) (string, string, string, error) {
	name = strings.TrimSpace(name)
	description = strings.TrimSpace(description)
	status = strings.TrimSpace(status)

	if name == "" {
		return "", "", "", validationError("name is required")
	}
	if len(name) > 160 {
		return "", "", "", validationError("name must be 160 characters or fewer")
	}
	normalizedStatus, ok := entity.NormalizeProjectStatus(status)
	if !ok {
		return "", "", "", validationError("invalid project status")
	}

	return name, description, normalizedStatus, nil
}

func validateProjectDates(startDate time.Time, estimatedEndDate time.Time, actualEndDate *time.Time) error {
	if startDate.IsZero() {
		return validationError("startDate is required")
	}
	if estimatedEndDate.IsZero() {
		return validationError("estimatedEndDate is required")
	}
	if dateBefore(estimatedEndDate, startDate) {
		return validationError("estimatedEndDate cannot be before startDate")
	}
	if actualEndDate != nil && dateBefore(*actualEndDate, startDate) {
		return validationError("actualEndDate cannot be before startDate")
	}

	return nil
}

func uniquePositiveIDs(ids []int) []int {
	seen := make(map[int]struct{}, len(ids))
	unique := make([]int, 0, len(ids))
	for _, id := range ids {
		if id <= 0 {
			continue
		}
		if _, exists := seen[id]; exists {
			continue
		}

		seen[id] = struct{}{}
		unique = append(unique, id)
	}

	return unique
}
