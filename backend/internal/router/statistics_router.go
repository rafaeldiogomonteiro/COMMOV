package router

import (
	"net/http"

	"commov/backend/internal/services"
	"github.com/go-chi/chi/v5"
)

type StatisticsRouter struct {
	UserReportService *services.UserReportService
	AuthService       *services.AuthService
}

func (r *StatisticsRouter) Register(chiRouter chi.Router) {
	chiRouter.Get("/statistics/users", r.listUsers)
	chiRouter.Get("/statistics/users/{userId}/export", r.exportUserReport)
	chiRouter.Get("/statistics/projects/{projectId}/export", r.exportProjectReport)
	chiRouter.Get("/statistics/projects/{projectId}/tasks/export", r.exportProjectTasksReport)
}

func (r *StatisticsRouter) listUsers(w http.ResponseWriter, req *http.Request) {
	actor, ok := requireAuthenticated(w, req, r.AuthService)
	if !ok {
		return
	}

	users, err := r.UserReportService.ListUsers(req.Context(), actor.UserID)
	if err != nil {
		writeUserServiceError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, users)
}

func (r *StatisticsRouter) exportUserReport(w http.ResponseWriter, req *http.Request) {
	actor, ok := requireAuthenticated(w, req, r.AuthService)
	if !ok {
		return
	}

	userID, err := intURLParam(req, "userId")
	if err != nil {
		writeError(w, http.StatusBadRequest, "userId is invalid")
		return
	}

	pdfBytes, filename, err := r.UserReportService.ExportPDF(req.Context(), actor.UserID, userID)
	if err != nil {
		writeUserServiceError(w, err)
		return
	}

	writePDFAttachment(w, pdfBytes, filename)
}

func (r *StatisticsRouter) exportProjectReport(w http.ResponseWriter, req *http.Request) {
	actor, ok := requireAuthenticated(w, req, r.AuthService)
	if !ok {
		return
	}

	projectID, err := intURLParam(req, "projectId")
	if err != nil {
		writeError(w, http.StatusBadRequest, "projectId is invalid")
		return
	}

	pdfBytes, filename, err := r.UserReportService.ExportProjectPDF(req.Context(), actor.UserID, projectID)
	if err != nil {
		writeUserServiceError(w, err)
		return
	}

	writePDFAttachment(w, pdfBytes, filename)
}

func (r *StatisticsRouter) exportProjectTasksReport(w http.ResponseWriter, req *http.Request) {
	actor, ok := requireAuthenticated(w, req, r.AuthService)
	if !ok {
		return
	}

	projectID, err := intURLParam(req, "projectId")
	if err != nil {
		writeError(w, http.StatusBadRequest, "projectId is invalid")
		return
	}

	pdfBytes, filename, err := r.UserReportService.ExportProjectTasksPDF(req.Context(), actor.UserID, projectID)
	if err != nil {
		writeUserServiceError(w, err)
		return
	}

	writePDFAttachment(w, pdfBytes, filename)
}

func writePDFAttachment(w http.ResponseWriter, pdfBytes []byte, filename string) {
	w.Header().Set("Content-Type", "application/pdf")
	w.Header().Set("Content-Disposition", "attachment; filename=\""+filename+"\"")
	w.WriteHeader(http.StatusOK)
	_, _ = w.Write(pdfBytes)
}
