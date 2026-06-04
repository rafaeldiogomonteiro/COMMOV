package router

import (
	"encoding/json"
	"net/http"

	"commov/backend/internal/services"
	"github.com/go-chi/chi/v5"
)

type ProjectRouter struct {
	ProjectService *services.ProjectService
	AuthService    *services.AuthService
}

func (r *ProjectRouter) Register(chiRouter chi.Router) {
	chiRouter.Get("/projects", r.list)
	chiRouter.Post("/projects", r.create)
	chiRouter.Get("/projects/{projectId}", r.get)
	chiRouter.Put("/projects/{projectId}", r.update)
	chiRouter.Patch("/projects/{projectId}", r.update)
	chiRouter.Delete("/projects/{projectId}", r.delete)
	chiRouter.Get("/projects/{projectId}/users", r.listMembers)
	chiRouter.Post("/projects/{projectId}/users", r.addMember)
	chiRouter.Delete("/projects/{projectId}/users/{userId}", r.removeMember)
}

func (r *ProjectRouter) list(w http.ResponseWriter, req *http.Request) {
	actor, ok := requireAuthenticated(w, req, r.AuthService)
	if !ok {
		return
	}

	projects, err := r.ProjectService.List(req.Context(), actor.UserID)
	if err != nil {
		writeUserServiceError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, projects)
}

func (r *ProjectRouter) get(w http.ResponseWriter, req *http.Request) {
	actor, ok := requireAuthenticated(w, req, r.AuthService)
	if !ok {
		return
	}

	projectID, err := intURLParam(req, "projectId")
	if err != nil {
		writeError(w, http.StatusBadRequest, "projectId is invalid")
		return
	}

	project, err := r.ProjectService.Get(req.Context(), actor.UserID, projectID)
	if err != nil {
		writeUserServiceError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, project)
}

func (r *ProjectRouter) create(w http.ResponseWriter, req *http.Request) {
	defer req.Body.Close()

	actor, ok := requireAuthenticated(w, req, r.AuthService)
	if !ok {
		return
	}

	var input struct {
		Name             string `json:"name"`
		Description      string `json:"description"`
		ManagerID        int    `json:"managerId"`
		StartDate        string `json:"startDate"`
		EstimatedEndDate string `json:"estimatedEndDate"`
		MemberIDs        []int  `json:"memberIds"`
	}
	if err := json.NewDecoder(req.Body).Decode(&input); err != nil {
		writeError(w, http.StatusBadRequest, "invalid request body")
		return
	}

	startDate, err := parseRequiredDate(input.StartDate, "startDate")
	if err != nil {
		writeError(w, http.StatusBadRequest, err.Error())
		return
	}
	estimatedEndDate, err := parseRequiredDate(input.EstimatedEndDate, "estimatedEndDate")
	if err != nil {
		writeError(w, http.StatusBadRequest, err.Error())
		return
	}
	project, err := r.ProjectService.Create(req.Context(), actor.UserID, services.ProjectCreateInput{
		Name:             input.Name,
		Description:      input.Description,
		ManagerID:        input.ManagerID,
		StartDate:        startDate,
		EstimatedEndDate: estimatedEndDate,
		MemberIDs:        input.MemberIDs,
	})
	if err != nil {
		writeUserServiceError(w, err)
		return
	}

	writeJSON(w, http.StatusCreated, project)
}

func (r *ProjectRouter) update(w http.ResponseWriter, req *http.Request) {
	defer req.Body.Close()

	actor, ok := requireAuthenticated(w, req, r.AuthService)
	if !ok {
		return
	}

	projectID, err := intURLParam(req, "projectId")
	if err != nil {
		writeError(w, http.StatusBadRequest, "projectId is invalid")
		return
	}

	var input struct {
		Name             *string `json:"name"`
		Description      *string `json:"description"`
		Status           *string `json:"status"`
		ManagerID        *int    `json:"managerId"`
		StartDate        *string `json:"startDate"`
		EstimatedEndDate *string `json:"estimatedEndDate"`
		ActualEndDate    *string `json:"actualEndDate"`
	}
	if err := json.NewDecoder(req.Body).Decode(&input); err != nil {
		writeError(w, http.StatusBadRequest, "invalid request body")
		return
	}

	startDate, err := parseDatePointer(input.StartDate, "startDate")
	if err != nil {
		writeError(w, http.StatusBadRequest, err.Error())
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

	project, err := r.ProjectService.Update(req.Context(), actor.UserID, projectID, services.ProjectUpdateInput{
		Name:             input.Name,
		Description:      input.Description,
		Status:           input.Status,
		ManagerID:        input.ManagerID,
		StartDate:        startDate,
		EstimatedEndDate: estimatedEndDate,
		ActualEndDate:    actualEndDate,
	})
	if err != nil {
		writeUserServiceError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, project)
}

func (r *ProjectRouter) delete(w http.ResponseWriter, req *http.Request) {
	actor, ok := requireAuthenticated(w, req, r.AuthService)
	if !ok {
		return
	}

	projectID, err := intURLParam(req, "projectId")
	if err != nil {
		writeError(w, http.StatusBadRequest, "projectId is invalid")
		return
	}

	if err := r.ProjectService.Delete(req.Context(), actor.UserID, projectID); err != nil {
		writeUserServiceError(w, err)
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

func (r *ProjectRouter) listMembers(w http.ResponseWriter, req *http.Request) {
	actor, ok := requireAuthenticated(w, req, r.AuthService)
	if !ok {
		return
	}

	projectID, err := intURLParam(req, "projectId")
	if err != nil {
		writeError(w, http.StatusBadRequest, "projectId is invalid")
		return
	}

	users, err := r.ProjectService.ListMembers(req.Context(), actor.UserID, projectID)
	if err != nil {
		writeUserServiceError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, users)
}

func (r *ProjectRouter) addMember(w http.ResponseWriter, req *http.Request) {
	defer req.Body.Close()

	actor, ok := requireAuthenticated(w, req, r.AuthService)
	if !ok {
		return
	}

	projectID, err := intURLParam(req, "projectId")
	if err != nil {
		writeError(w, http.StatusBadRequest, "projectId is invalid")
		return
	}

	var input struct {
		UserID int `json:"userId"`
	}
	if err := json.NewDecoder(req.Body).Decode(&input); err != nil {
		writeError(w, http.StatusBadRequest, "invalid request body")
		return
	}

	if err := r.ProjectService.AddMember(req.Context(), actor.UserID, projectID, input.UserID); err != nil {
		writeUserServiceError(w, err)
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

func (r *ProjectRouter) removeMember(w http.ResponseWriter, req *http.Request) {
	actor, ok := requireAuthenticated(w, req, r.AuthService)
	if !ok {
		return
	}

	projectID, err := intURLParam(req, "projectId")
	if err != nil {
		writeError(w, http.StatusBadRequest, "projectId is invalid")
		return
	}
	userID, err := intURLParam(req, "userId")
	if err != nil {
		writeError(w, http.StatusBadRequest, "userId is invalid")
		return
	}

	if err := r.ProjectService.RemoveMember(req.Context(), actor.UserID, projectID, userID); err != nil {
		writeUserServiceError(w, err)
		return
	}

	w.WriteHeader(http.StatusNoContent)
}
