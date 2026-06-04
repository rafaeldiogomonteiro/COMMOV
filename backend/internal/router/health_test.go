package router

import (
	"log"
	"net/http"
	"net/http/httptest"
	"testing"

	"commov/backend/internal/services"
)

func TestHealthEndpoint(t *testing.T) {
	authService := &services.AuthService{}
	handler := NewRouter(Dependencies{
		Logger:         log.Default(),
		AuthRouter:     &AuthRouter{AuthService: authService},
		UserRouter:     &UserRouter{UserService: &services.UserService{}, AuthService: authService},
		ProjectRouter:  &ProjectRouter{ProjectService: &services.ProjectService{}, AuthService: authService},
		TaskRouter:     &TaskRouter{TaskService: &services.TaskService{}, AuthService: authService},
	})

	req := httptest.NewRequest(http.MethodGet, "/health", nil)
	rec := httptest.NewRecorder()
	handler.ServeHTTP(rec, req)

	if rec.Code != http.StatusOK {
		t.Fatalf("status = %d, want %d", rec.Code, http.StatusOK)
	}
}
