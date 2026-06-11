package router

import (
	"encoding/json"
	"errors"
	"io"
	"net/http"

	"commov/backend/internal/services"
	"github.com/go-chi/chi/v5"
)

type TaskRouter struct {
	TaskService *services.TaskService
	AuthService *services.AuthService
}

func (r *TaskRouter) Register(chiRouter chi.Router) {
	chiRouter.Get("/tasks", r.list)
	chiRouter.Post("/tasks", r.create)
	chiRouter.Get("/tasks/{taskId}", r.get)
	chiRouter.Put("/tasks/{taskId}", r.update)
	chiRouter.Patch("/tasks/{taskId}", r.update)
	chiRouter.Delete("/tasks/{taskId}", r.delete)
	chiRouter.Post("/tasks/{taskId}/time-spent", r.addTimeSpent)
	chiRouter.Get("/tasks/{taskId}/time-entries", r.listTimeEntries)
	chiRouter.Post("/tasks/{taskId}/complete", r.complete)
	chiRouter.Patch("/tasks/{taskId}/complete", r.complete)
	chiRouter.Post("/tasks/{taskId}/users", r.addAssignee)
	chiRouter.Delete("/tasks/{taskId}/users/{userId}", r.removeAssignee)
	chiRouter.Get("/projects/{projectId}/tasks", r.listByProject)
	chiRouter.Post("/projects/{projectId}/tasks", r.create)
}

func (r *TaskRouter) list(w http.ResponseWriter, req *http.Request) {
	actor, ok := requireAuthenticated(w, req, r.AuthService)
	if !ok {
		return
	}

	tasks, err := r.TaskService.List(req.Context(), actor.UserID)
	if err != nil {
		writeUserServiceError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, tasks)
}

func (r *TaskRouter) listByProject(w http.ResponseWriter, req *http.Request) {
	actor, ok := requireAuthenticated(w, req, r.AuthService)
	if !ok {
		return
	}

	projectID, err := intURLParam(req, "projectId")
	if err != nil {
		writeError(w, http.StatusBadRequest, "projectId is invalid")
		return
	}

	tasks, err := r.TaskService.ListByProject(req.Context(), actor.UserID, projectID)
	if err != nil {
		writeUserServiceError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, tasks)
}

func (r *TaskRouter) get(w http.ResponseWriter, req *http.Request) {
	actor, ok := requireAuthenticated(w, req, r.AuthService)
	if !ok {
		return
	}

	taskID, err := intURLParam(req, "taskId")
	if err != nil {
		writeError(w, http.StatusBadRequest, "taskId is invalid")
		return
	}

	task, err := r.TaskService.Get(req.Context(), actor.UserID, taskID)
	if err != nil {
		writeUserServiceError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, task)
}

func (r *TaskRouter) create(w http.ResponseWriter, req *http.Request) {
	defer req.Body.Close()

	actor, ok := requireAuthenticated(w, req, r.AuthService)
	if !ok {
		return
	}

	var input struct {
		ProjectID        int     `json:"projectId"`
		UserID           int     `json:"userId"`
		UserIDs          []int   `json:"userIds"`
		Title            string  `json:"title"`
		Description      string  `json:"description"`
		EstimatedEndDate string  `json:"estimatedEndDate"`
		EstimatedTime    float64 `json:"estimatedTime"`
		Location         string  `json:"location"`
	}
	if err := json.NewDecoder(req.Body).Decode(&input); err != nil {
		writeError(w, http.StatusBadRequest, "invalid request body")
		return
	}

	if urlProjectID := chi.URLParam(req, "projectId"); urlProjectID != "" {
		projectID, err := intURLParam(req, "projectId")
		if err != nil {
			writeError(w, http.StatusBadRequest, "projectId is invalid")
			return
		}
		input.ProjectID = projectID
	}

	estimatedEndDate, err := parseRequiredDate(input.EstimatedEndDate, "estimatedEndDate")
	if err != nil {
		writeError(w, http.StatusBadRequest, err.Error())
		return
	}

	userIDs := input.UserIDs
	if len(userIDs) == 0 && input.UserID > 0 {
		userIDs = []int{input.UserID}
	}

	task, err := r.TaskService.Create(req.Context(), actor.UserID, services.TaskCreateInput{
		ProjectID:        input.ProjectID,
		UserIDs:          userIDs,
		Title:            input.Title,
		Description:      input.Description,
		EstimatedEndDate: &estimatedEndDate,
		EstimatedTime:    input.EstimatedTime,
		Location:         input.Location,
	})
	if err != nil {
		writeUserServiceError(w, err)
		return
	}

	writeJSON(w, http.StatusCreated, task)
}

func (r *TaskRouter) update(w http.ResponseWriter, req *http.Request) {
	defer req.Body.Close()

	actor, ok := requireAuthenticated(w, req, r.AuthService)
	if !ok {
		return
	}

	taskID, err := intURLParam(req, "taskId")
	if err != nil {
		writeError(w, http.StatusBadRequest, "taskId is invalid")
		return
	}

	var input struct {
		ProjectID        *int     `json:"projectId"`
		Title            *string  `json:"title"`
		Description      *string  `json:"description"`
		Status           *string  `json:"status"`
		EstimatedEndDate *string  `json:"estimatedEndDate"`
		ActualEndDate    *string  `json:"actualEndDate"`
		EstimatedTime    *float64 `json:"estimatedTime"`
		TimeSpent        *float64 `json:"timeSpent"`
		CompletionRate   *float64 `json:"completionRate"`
		WorkDate         *string  `json:"workDate"`
		Location         *string  `json:"location"`
		Observation      *string  `json:"observation"`
		Photo            *string  `json:"photo"`
	}
	if err := json.NewDecoder(req.Body).Decode(&input); err != nil {
		writeError(w, http.StatusBadRequest, "invalid request body")
		return
	}

	estimatedEndDate, err := parseDatePointer(input.EstimatedEndDate, "estimatedEndDate")
	if err != nil {
		writeError(w, http.StatusBadRequest, err.Error())
		return
	}
	actualEndDate, err := parseDatePointer(input.ActualEndDate, "actualEndDate")
	if err != nil {
		writeError(w, http.StatusBadRequest, err.Error())
		return
	}
	workDate, err := parseDatePointer(input.WorkDate, "workDate")
	if err != nil {
		writeError(w, http.StatusBadRequest, err.Error())
		return
	}

	task, err := r.TaskService.Update(req.Context(), actor.UserID, taskID, services.TaskUpdateInput{
		ProjectID:        input.ProjectID,
		Title:            input.Title,
		Description:      input.Description,
		Status:           input.Status,
		EstimatedEndDate: estimatedEndDate,
		ActualEndDate:    actualEndDate,
		EstimatedTime:    input.EstimatedTime,
		TimeSpent:        input.TimeSpent,
		CompletionRate:   input.CompletionRate,
		WorkDate:         workDate,
		Location:         input.Location,
		Observation:      input.Observation,
		Photo:            input.Photo,
	})
	if err != nil {
		writeUserServiceError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, task)
}

func (r *TaskRouter) delete(w http.ResponseWriter, req *http.Request) {
	actor, ok := requireAuthenticated(w, req, r.AuthService)
	if !ok {
		return
	}

	taskID, err := intURLParam(req, "taskId")
	if err != nil {
		writeError(w, http.StatusBadRequest, "taskId is invalid")
		return
	}

	if err := r.TaskService.Delete(req.Context(), actor.UserID, taskID); err != nil {
		writeUserServiceError(w, err)
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

func (r *TaskRouter) listTimeEntries(w http.ResponseWriter, req *http.Request) {
	actor, ok := requireAuthenticated(w, req, r.AuthService)
	if !ok {
		return
	}

	taskID, err := intURLParam(req, "taskId")
	if err != nil {
		writeError(w, http.StatusBadRequest, "taskId is invalid")
		return
	}

	entries, err := r.TaskService.ListTimeEntries(req.Context(), actor.UserID, taskID)
	if err != nil {
		writeUserServiceError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, entries)
}

func (r *TaskRouter) addTimeSpent(w http.ResponseWriter, req *http.Request) {
	defer req.Body.Close()

	actor, ok := requireAuthenticated(w, req, r.AuthService)
	if !ok {
		return
	}

	taskID, err := intURLParam(req, "taskId")
	if err != nil {
		writeError(w, http.StatusBadRequest, "taskId is invalid")
		return
	}

	var input struct {
		TimeSpent   float64 `json:"timeSpent"`
		WorkDate    *string `json:"workDate"`
		Observation *string `json:"observation"`
		Photo       *string `json:"photo"`
	}
	if err := json.NewDecoder(req.Body).Decode(&input); err != nil {
		writeError(w, http.StatusBadRequest, "invalid request body")
		return
	}

	workDate, err := parseDatePointer(input.WorkDate, "workDate")
	if err != nil {
		writeError(w, http.StatusBadRequest, err.Error())
		return
	}

	task, err := r.TaskService.AddTimeSpent(req.Context(), actor.UserID, taskID, services.TaskAddTimeSpentInput{
		TimeSpent:   input.TimeSpent,
		WorkDate:    workDate,
		Observation: input.Observation,
		Photo:       input.Photo,
	})
	if err != nil {
		writeUserServiceError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, task)
}

func (r *TaskRouter) complete(w http.ResponseWriter, req *http.Request) {
	defer req.Body.Close()

	actor, ok := requireAuthenticated(w, req, r.AuthService)
	if !ok {
		return
	}

	taskID, err := intURLParam(req, "taskId")
	if err != nil {
		writeError(w, http.StatusBadRequest, "taskId is invalid")
		return
	}

	var input struct {
		TimeSpent   *float64 `json:"timeSpent"`
		WorkDate    *string  `json:"workDate"`
		Location    *string  `json:"location"`
		Observation *string  `json:"observation"`
		Photo       *string  `json:"photo"`
	}
	if err := json.NewDecoder(req.Body).Decode(&input); err != nil && !errors.Is(err, io.EOF) {
		writeError(w, http.StatusBadRequest, "invalid request body")
		return
	}

	workDate, err := parseDatePointer(input.WorkDate, "workDate")
	if err != nil {
		writeError(w, http.StatusBadRequest, err.Error())
		return
	}

	task, err := r.TaskService.Complete(req.Context(), actor.UserID, taskID, services.TaskCompleteInput{
		TimeSpent:   input.TimeSpent,
		WorkDate:    workDate,
		Location:    input.Location,
		Observation: input.Observation,
		Photo:       input.Photo,
	})
	if err != nil {
		writeUserServiceError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, task)
}

func (r *TaskRouter) addAssignee(w http.ResponseWriter, req *http.Request) {
	defer req.Body.Close()

	actor, ok := requireAuthenticated(w, req, r.AuthService)
	if !ok {
		return
	}

	taskID, err := intURLParam(req, "taskId")
	if err != nil {
		writeError(w, http.StatusBadRequest, "taskId is invalid")
		return
	}

	var input struct {
		UserID int `json:"userId"`
	}
	if err := json.NewDecoder(req.Body).Decode(&input); err != nil {
		writeError(w, http.StatusBadRequest, "invalid request body")
		return
	}

	task, err := r.TaskService.AddAssignee(req.Context(), actor.UserID, taskID, input.UserID)
	if err != nil {
		writeUserServiceError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, task)
}

func (r *TaskRouter) removeAssignee(w http.ResponseWriter, req *http.Request) {
	actor, ok := requireAuthenticated(w, req, r.AuthService)
	if !ok {
		return
	}

	taskID, err := intURLParam(req, "taskId")
	if err != nil {
		writeError(w, http.StatusBadRequest, "taskId is invalid")
		return
	}
	userID, err := intURLParam(req, "userId")
	if err != nil {
		writeError(w, http.StatusBadRequest, "userId is invalid")
		return
	}

	if err := r.TaskService.RemoveAssignee(req.Context(), actor.UserID, taskID, userID); err != nil {
		writeUserServiceError(w, err)
		return
	}

	w.WriteHeader(http.StatusNoContent)
}
