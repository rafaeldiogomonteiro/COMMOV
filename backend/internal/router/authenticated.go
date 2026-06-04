package router

import (
	"net/http"

	"commov/backend/internal/entity"
	"commov/backend/internal/services"
)

func requireAuthenticated(w http.ResponseWriter, req *http.Request, authService *services.AuthService) (*entity.User, bool) {
	actor, loggedIn, err := authService.CheckLogin(req.Context(), authToken(req))
	if err != nil {
		writeUserServiceError(w, err)
		return nil, false
	}
	if !loggedIn {
		writeError(w, http.StatusUnauthorized, "invalid token")
		return nil, false
	}

	return actor, true
}
