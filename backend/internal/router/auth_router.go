package router

import (
	"encoding/json"
	"errors"
	"log"
	"net/http"
	"strings"

	"commov/backend/internal/services"
	"github.com/go-chi/chi/v5"
)

type AuthRouter struct {
	AuthService *services.AuthService
	UserService *services.UserService
}

func (r *AuthRouter) Register(chiRouter chi.Router) {
	chiRouter.Post("/login", r.login)
	chiRouter.Post("/logout", r.logout)
	chiRouter.Get("/check-login", r.checkLogin)
	chiRouter.Post("/register", r.register)
}

func (r *AuthRouter) login(w http.ResponseWriter, req *http.Request) {
	defer req.Body.Close()

	var input struct {
		Email    string `json:"email"`
		Password string `json:"password"`
	}
	if err := json.NewDecoder(req.Body).Decode(&input); err != nil {
		writeError(w, http.StatusBadRequest, "invalid request body")
		return
	}

	token, user, err := r.AuthService.Login(req.Context(), input.Email, input.Password)
	if err != nil {
		writeAuthServiceError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, map[string]any{
		"token": token,
		"user":  user,
	})
}

func (r *AuthRouter) logout(w http.ResponseWriter, req *http.Request) {
	token := authToken(req)
	if token == "" {
		writeError(w, http.StatusUnauthorized, "token is required")
		return
	}

	r.AuthService.Logout(token)
	writeJSON(w, http.StatusOK, map[string]string{"status": "ok"})
}

func (r *AuthRouter) checkLogin(w http.ResponseWriter, req *http.Request) {
	user, loggedIn, err := r.AuthService.CheckLogin(req.Context(), authToken(req))
	if err != nil {
		writeAuthServiceError(w, err)
		return
	}

	if !loggedIn {
		writeJSON(w, http.StatusOK, map[string]bool{"loggedIn": false})
		return
	}

	writeJSON(w, http.StatusOK, map[string]any{
		"loggedIn": true,
		"user":     user,
	})
}

func authToken(req *http.Request) string {
	authHeader := strings.TrimSpace(req.Header.Get("Authorization"))
	if strings.HasPrefix(strings.ToLower(authHeader), "bearer ") {
		return strings.TrimSpace(authHeader[7:])
	}
	if authHeader != "" {
		return authHeader
	}

	return strings.TrimSpace(req.Header.Get("X-Auth-Token"))
}

func writeAuthServiceError(w http.ResponseWriter, err error) {
	message := cleanServiceErrorMessage(err)

	switch {
	case errors.Is(err, services.ErrUnauthorized):
		writeError(w, http.StatusUnauthorized, message)
	case errors.Is(err, services.ErrValidation):
		writeError(w, http.StatusBadRequest, message)
	default:
		writeError(w, http.StatusInternalServerError, "internal server error")
	}
}

func (r *AuthRouter) register(w http.ResponseWriter, req *http.Request) {
	defer req.Body.Close()

	var input struct {
		Name     string `json:"name"`
		Username string `json:"username"`
		Email    string `json:"email"`
		Password string `json:"password"`
	}
	if err := json.NewDecoder(req.Body).Decode(&input); err != nil {
		writeError(w, http.StatusBadRequest, "invalid request body")
		return
	}

	if r.UserService == nil {
		writeError(w, http.StatusInternalServerError, "service unavailable")
		return
	}

	user, err := r.UserService.RegisterPublic(req.Context(), input.Name, input.Username, input.Email, input.Password)
	if err != nil {
		writeRegisterError(w, err)
		return
	}

	writeJSON(w, http.StatusCreated, map[string]any{
		"user": user,
	})
}

func writeRegisterError(w http.ResponseWriter, err error) {
	message := cleanServiceErrorMessage(err)

	switch {
	case errors.Is(err, services.ErrValidation):
		writeError(w, http.StatusBadRequest, message)
	case errors.Is(err, services.ErrConflict):
		writeError(w, http.StatusConflict, message)
	default:
		// Surface the real cause on 5xx for easier diagnosis during development.
		// In a production deployment you may want to log only and return a generic message.
		log.Printf("register unexpected error: %v", err)
		writeError(w, http.StatusInternalServerError, message)
	}
}
