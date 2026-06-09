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

const (
	defaultTaskStatus   = entity.TaskStatusPending
	completedTaskStatus = entity.TaskStatusCompleted
)

type TaskCreateInput struct {
	ProjectID        int
	UserID           int
	Title            string
	Description      string
	EstimatedEndDate *time.Time
	EstimatedTime    float64
	Location         string
}

type TaskUpdateInput struct {
	ProjectID        *int
	UserID           *int
	Title            *string
	Description      *string
	Status           *string
	EstimatedEndDate *time.Time
	ActualEndDate    *time.Time
	EstimatedTime    *float64
	TimeSpent        *float64
	CompletionRate   *float64
	WorkDate         *time.Time
	Location         *string
	Observation      *string
	Photo            *string
}

type TaskCompleteInput struct {
	TimeSpent   *float64
	WorkDate    *time.Time
	Location    *string
	Observation *string
	Photo       *string
}

type TaskAddTimeSpentInput struct {
	TimeSpent   float64
	WorkDate    *time.Time
	Observation *string
}

type TaskService struct {
	TaskRepo           *postgres.TaskRepo
	TaskTimeEntryRepo  *postgres.TaskTimeEntryRepo
	ProjectRepo        *postgres.ProjectRepo
	ProjectUserRepo    *postgres.ProjectUserRepo
	UserRepo           *postgres.UserRepo
}

func (s *TaskService) List(ctx context.Context, actorUserID int) ([]entity.Task, error) {
	actor, err := authenticatedActor(ctx, s.UserRepo, actorUserID)
	if err != nil {
		return nil, err
	}

	if hasManagementRole(actor) {
		tasks, err := s.TaskRepo.List(ctx)
		if err != nil {
			return nil, fmt.Errorf("list tasks: %w", err)
		}

		return tasks, nil
	}

	tasks, err := s.TaskRepo.ListByUserID(ctx, actor.UserID)
	if err != nil {
		return nil, fmt.Errorf("list user tasks: %w", err)
	}

	return tasks, nil
}

func (s *TaskService) ListByProject(ctx context.Context, actorUserID int, projectID int) ([]entity.Task, error) {
	actor, err := authenticatedActor(ctx, s.UserRepo, actorUserID)
	if err != nil {
		return nil, err
	}
	if _, err := s.getProject(ctx, projectID); err != nil {
		return nil, err
	}

	if hasManagementRole(actor) {
		tasks, err := s.TaskRepo.ListByProjectID(ctx, projectID)
		if err != nil {
			return nil, fmt.Errorf("list project tasks: %w", err)
		}

		return tasks, nil
	}

	isMember, err := s.ProjectUserRepo.ExistsByProjectAndUser(ctx, projectID, actor.UserID)
	if err != nil {
		return nil, fmt.Errorf("check project membership: %w", err)
	}
	if !isMember {
		return nil, fmt.Errorf("%w: project is not assigned to this user", ErrForbidden)
	}

	tasks, err := s.TaskRepo.ListByProjectAndUserID(ctx, projectID, actor.UserID)
	if err != nil {
		return nil, fmt.Errorf("list user project tasks: %w", err)
	}

	return tasks, nil
}

func (s *TaskService) Get(ctx context.Context, actorUserID int, taskID int) (*entity.Task, error) {
	actor, err := authenticatedActor(ctx, s.UserRepo, actorUserID)
	if err != nil {
		return nil, err
	}

	task, err := s.getTask(ctx, taskID)
	if err != nil {
		return nil, err
	}
	if err := s.ensureTaskAccess(ctx, actor, task); err != nil {
		return nil, err
	}

	return task, nil
}

func (s *TaskService) ListTimeEntries(ctx context.Context, actorUserID int, taskID int) ([]entity.TaskTimeEntryView, error) {
	actor, err := authenticatedActor(ctx, s.UserRepo, actorUserID)
	if err != nil {
		return nil, err
	}

	task, err := s.getTask(ctx, taskID)
	if err != nil {
		return nil, err
	}
	if err := s.ensureTaskAccess(ctx, actor, task); err != nil {
		return nil, err
	}

	entries, err := s.TaskTimeEntryRepo.ListByTaskID(ctx, taskID)
	if err != nil {
		return nil, fmt.Errorf("list task time entries: %w", err)
	}

	return entries, nil
}

func (s *TaskService) Create(ctx context.Context, actorUserID int, input TaskCreateInput) (*entity.Task, error) {
	if _, err := actorWithManagementRole(ctx, s.UserRepo, actorUserID); err != nil {
		return nil, err
	}
	project, err := s.getProject(ctx, input.ProjectID)
	if err != nil {
		return nil, err
	}
	if _, err := s.getActiveUser(ctx, input.UserID, "userId"); err != nil {
		return nil, err
	}

	title, description, status, location, observation, photo, err := normalizeTaskText(
		input.Title,
		input.Description,
		defaultTaskStatus,
		input.Location,
		"",
		"",
	)
	if err != nil {
		return nil, err
	}
	if err := validateTaskNumbers(input.EstimatedTime, 0, 0); err != nil {
		return nil, err
	}
	if err := validateTaskDates(project, input.EstimatedEndDate, nil, nil); err != nil {
		return nil, err
	}

	task := &entity.Task{
		ProjectID:        input.ProjectID,
		UserID:           input.UserID,
		Title:            title,
		Description:      description,
		Status:           status,
		EstimatedEndDate: input.EstimatedEndDate,
		EstimatedTime:    input.EstimatedTime,
		Location:         location,
		Observation:      observation,
		Photo:            photo,
	}

	if err := s.ensureProjectMember(ctx, task.ProjectID, task.UserID); err != nil {
		return nil, err
	}
	if err := s.TaskRepo.Create(ctx, task); err != nil {
		return nil, fmt.Errorf("create task: %w", err)
	}

	return task, nil
}

func (s *TaskService) Update(ctx context.Context, actorUserID int, taskID int, input TaskUpdateInput) (*entity.Task, error) {
	if _, err := actorWithManagementRole(ctx, s.UserRepo, actorUserID); err != nil {
		return nil, err
	}

	task, err := s.getTask(ctx, taskID)
	if err != nil {
		return nil, err
	}

	if input.ProjectID != nil {
		if _, err := s.getProject(ctx, *input.ProjectID); err != nil {
			return nil, err
		}
		task.ProjectID = *input.ProjectID
	}
	if input.UserID != nil {
		if _, err := s.getActiveUser(ctx, *input.UserID, "userId"); err != nil {
			return nil, err
		}
		task.UserID = *input.UserID
	}
	if input.Title != nil {
		title := strings.TrimSpace(*input.Title)
		if title == "" {
			return nil, validationError("title is required")
		}
		if len(title) > 160 {
			return nil, validationError("title must be 160 characters or fewer")
		}
		task.Title = title
	}
	if input.Description != nil {
		task.Description = strings.TrimSpace(*input.Description)
	}
	previousStatus := task.Status
	if input.Status != nil {
		status, ok := entity.NormalizeTaskStatus(*input.Status)
		if !ok {
			return nil, validationError("invalid task status")
		}
		task.Status = status
	}
	if input.EstimatedEndDate != nil {
		task.EstimatedEndDate = input.EstimatedEndDate
	}
	if input.ActualEndDate != nil {
		task.ActualEndDate = input.ActualEndDate
	}
	if input.EstimatedTime != nil {
		if *input.EstimatedTime < 0 {
			return nil, validationError("estimatedTime cannot be negative")
		}
		task.EstimatedTime = *input.EstimatedTime
	}
	if input.TimeSpent != nil {
		if *input.TimeSpent < 0 {
			return nil, validationError("timeSpent cannot be negative")
		}
		task.TimeSpent = *input.TimeSpent
	}
	if input.CompletionRate != nil {
		if *input.CompletionRate < 0 || *input.CompletionRate > 100 {
			return nil, validationError("completionRate must be between 0 and 100")
		}
		task.CompletionRate = *input.CompletionRate
	}
	if input.WorkDate != nil {
		task.WorkDate = input.WorkDate
	}
	if input.Location != nil {
		location := strings.TrimSpace(*input.Location)
		if len(location) > 160 {
			return nil, validationError("location must be 160 characters or fewer")
		}
		task.Location = location
	}
	if input.Observation != nil {
		task.Observation = strings.TrimSpace(*input.Observation)
	}
	if input.Photo != nil {
		task.Photo = strings.TrimSpace(*input.Photo)
	}
	if entity.IsCompletedStatus(task.Status) {
		task.CompletionRate = 100
		if task.ActualEndDate == nil {
			today := currentDate()
			task.ActualEndDate = &today
		}
	} else if entity.IsCompletedStatus(previousStatus) {
		task.ActualEndDate = nil
		if task.CompletionRate >= 100 {
			task.CompletionRate = 0
		}
	}

	project, err := s.getProject(ctx, task.ProjectID)
	if err != nil {
		return nil, err
	}
	if err := validateTaskDates(project, task.EstimatedEndDate, task.ActualEndDate, task.WorkDate); err != nil {
		return nil, err
	}

	if err := s.ensureProjectMember(ctx, task.ProjectID, task.UserID); err != nil {
		return nil, err
	}
	if err := s.TaskRepo.Update(ctx, task); err != nil {
		return nil, fmt.Errorf("update task: %w", err)
	}

	return task, nil
}

func (s *TaskService) Delete(ctx context.Context, actorUserID int, taskID int) error {
	if _, err := actorWithManagementRole(ctx, s.UserRepo, actorUserID); err != nil {
		return err
	}
	if _, err := s.getTask(ctx, taskID); err != nil {
		return err
	}

	if err := s.TaskRepo.Delete(ctx, taskID); err != nil {
		return fmt.Errorf("delete task: %w", err)
	}

	return nil
}

func (s *TaskService) AddTimeSpent(ctx context.Context, actorUserID int, taskID int, input TaskAddTimeSpentInput) (*entity.Task, error) {
	actor, err := authenticatedActor(ctx, s.UserRepo, actorUserID)
	if err != nil {
		return nil, err
	}

	task, err := s.getTask(ctx, taskID)
	if err != nil {
		return nil, err
	}
	if err := s.ensureTaskTimeLogAccess(ctx, actor, task); err != nil {
		return nil, err
	}
	if entity.IsCompletedStatus(task.Status) {
		return nil, validationError("completed tasks cannot receive additional time")
	}
	if input.TimeSpent <= 0 {
		return nil, validationError("timeSpent must be greater than 0")
	}

	observation := ""
	if input.Observation != nil {
		observation = strings.TrimSpace(*input.Observation)
	}
	if observation == "" {
		return nil, validationError("observation is required")
	}

	project, err := s.getProject(ctx, task.ProjectID)
	if err != nil {
		return nil, err
	}

	workDate := currentDate()
	if input.WorkDate != nil {
		workDate = dateOnly(*input.WorkDate)
	}
	if err := validateWorkDate(project, workDate); err != nil {
		return nil, err
	}

	entry := &entity.TaskTimeEntry{
		TaskID:      task.TaskID,
		UserID:      actor.UserID,
		TimeSpent:   input.TimeSpent,
		WorkDate:    workDate,
		Observation: observation,
	}
	if err := s.TaskTimeEntryRepo.Create(ctx, entry); err != nil {
		return nil, fmt.Errorf("create task time entry: %w", err)
	}

	task.TimeSpent += input.TimeSpent
	task.WorkDate = &workDate
	if err := s.TaskRepo.Update(ctx, task); err != nil {
		return nil, fmt.Errorf("add task time spent: %w", err)
	}

	return task, nil
}

func (s *TaskService) Complete(ctx context.Context, actorUserID int, taskID int, input TaskCompleteInput) (*entity.Task, error) {
	actor, err := authenticatedActor(ctx, s.UserRepo, actorUserID)
	if err != nil {
		return nil, err
	}

	task, err := s.getTask(ctx, taskID)
	if err != nil {
		return nil, err
	}
	if !hasManagementRole(actor) && task.UserID != actor.UserID {
		return nil, fmt.Errorf("%w: task is not assigned to this user", ErrForbidden)
	}

	if input.TimeSpent != nil {
		if *input.TimeSpent < 0 {
			return nil, validationError("timeSpent cannot be negative")
		}
		task.TimeSpent = *input.TimeSpent
	}
	if input.WorkDate != nil {
		task.WorkDate = input.WorkDate
	} else if task.WorkDate == nil {
		today := currentDate()
		task.WorkDate = &today
	}
	if input.Location != nil {
		location := strings.TrimSpace(*input.Location)
		if len(location) > 160 {
			return nil, validationError("location must be 160 characters or fewer")
		}
		task.Location = location
	}
	if input.Observation != nil {
		task.Observation = strings.TrimSpace(*input.Observation)
	}
	if input.Photo != nil {
		task.Photo = strings.TrimSpace(*input.Photo)
	}

	project, err := s.getProject(ctx, task.ProjectID)
	if err != nil {
		return nil, err
	}

	today := currentDate()
	if err := validateTaskDates(project, task.EstimatedEndDate, &today, task.WorkDate); err != nil {
		return nil, err
	}

	task.Status = completedTaskStatus
	task.CompletionRate = 100
	task.ActualEndDate = &today

	if err := s.TaskRepo.Update(ctx, task); err != nil {
		return nil, fmt.Errorf("complete task: %w", err)
	}

	return task, nil
}

func (s *TaskService) getTask(ctx context.Context, taskID int) (*entity.Task, error) {
	if taskID <= 0 {
		return nil, validationError("taskId is invalid")
	}

	task, err := s.TaskRepo.GetByID(ctx, taskID)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, notFoundError("task not found")
		}
		return nil, fmt.Errorf("get task: %w", err)
	}

	return task, nil
}

func (s *TaskService) getProject(ctx context.Context, projectID int) (*entity.Project, error) {
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

func (s *TaskService) getActiveUser(ctx context.Context, userID int, fieldName string) (*entity.User, error) {
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

func (s *TaskService) ensureTaskAccess(ctx context.Context, actor *entity.User, task *entity.Task) error {
	if hasManagementRole(actor) || task.UserID == actor.UserID {
		return nil
	}

	isMember, err := s.ProjectUserRepo.ExistsByProjectAndUser(ctx, task.ProjectID, actor.UserID)
	if err != nil {
		return fmt.Errorf("check project membership: %w", err)
	}
	if isMember {
		return nil
	}

	return fmt.Errorf("%w: task is not accessible to this user", ErrForbidden)
}

func (s *TaskService) ensureTaskTimeLogAccess(ctx context.Context, actor *entity.User, task *entity.Task) error {
	return s.ensureTaskAccess(ctx, actor, task)
}

func (s *TaskService) ensureProjectMember(ctx context.Context, projectID int, userID int) error {
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

func normalizeTaskText(
	title string,
	description string,
	status string,
	location string,
	observation string,
	photo string,
) (string, string, string, string, string, string, error) {
	title = strings.TrimSpace(title)
	description = strings.TrimSpace(description)
	status = strings.TrimSpace(status)
	location = strings.TrimSpace(location)
	observation = strings.TrimSpace(observation)
	photo = strings.TrimSpace(photo)

	if title == "" {
		return "", "", "", "", "", "", validationError("title is required")
	}
	if len(title) > 160 {
		return "", "", "", "", "", "", validationError("title must be 160 characters or fewer")
	}
	normalizedStatus, ok := entity.NormalizeTaskStatus(status)
	if !ok {
		return "", "", "", "", "", "", validationError("invalid task status")
	}
	status = normalizedStatus
	if len(location) > 160 {
		return "", "", "", "", "", "", validationError("location must be 160 characters or fewer")
	}

	return title, description, status, location, observation, photo, nil
}

func validateTaskNumbers(estimatedTime float64, timeSpent float64, completionRate float64) error {
	if estimatedTime < 0 {
		return validationError("estimatedTime cannot be negative")
	}
	if timeSpent < 0 {
		return validationError("timeSpent cannot be negative")
	}
	if completionRate < 0 || completionRate > 100 {
		return validationError("completionRate must be between 0 and 100")
	}

	return nil
}

func validateWorkDate(project *entity.Project, workDate time.Time) error {
	if project == nil {
		return validationError("projectId is invalid")
	}
	if dateBefore(workDate, project.StartDate) {
		return validationError("workDate cannot be before project startDate")
	}

	return nil
}

func validateTaskDates(project *entity.Project, estimatedEndDate *time.Time, actualEndDate *time.Time, workDate *time.Time) error {
	if project == nil {
		return validationError("projectId is invalid")
	}
	if estimatedEndDate == nil {
		return validationError("estimatedEndDate is required")
	}
	if dateBefore(*estimatedEndDate, project.StartDate) {
		return validationError("estimatedEndDate cannot be before project startDate")
	}
	if dateAfter(*estimatedEndDate, project.EstimatedEndDate) {
		return validationError("estimatedEndDate cannot be after project estimatedEndDate")
	}
	if actualEndDate != nil && dateBefore(*actualEndDate, project.StartDate) {
		return validationError("actualEndDate cannot be before project startDate")
	}
	if workDate != nil && dateBefore(*workDate, project.StartDate) {
		return validationError("workDate cannot be before project startDate")
	}
	if actualEndDate != nil && workDate != nil && dateBefore(*actualEndDate, *workDate) {
		return validationError("actualEndDate cannot be before workDate")
	}

	return nil
}

func currentDate() time.Time {
	now := time.Now().UTC()
	return time.Date(now.Year(), now.Month(), now.Day(), 0, 0, 0, 0, time.UTC)
}

func dateOnly(value time.Time) time.Time {
	year, month, day := value.Date()
	return time.Date(year, month, day, 0, 0, 0, 0, time.UTC)
}

func dateBefore(left time.Time, right time.Time) bool {
	return dateOnly(left).Before(dateOnly(right))
}

func dateAfter(left time.Time, right time.Time) bool {
	return dateOnly(left).After(dateOnly(right))
}
