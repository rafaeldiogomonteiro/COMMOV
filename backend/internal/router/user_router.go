package router

import (
	"encoding/json"
	"net/http"

	"commov/backend/internal/services"
	"github.com/go-chi/chi/v5"
)

type UserRouter struct {
	UserService *services.UserService
}

func (r *UserRouter) Register(chiRouter chi.Router) {
	chiRouter.Post("/users", r.create)
}

func (r *UserRouter) create(w http.ResponseWriter, req *http.Request) {
	defer req.Body.Close()

	var input services.CreateUserInput
	if err := json.NewDecoder(req.Body).Decode(&input); err != nil {
		writeError(w, http.StatusBadRequest, "invalid request body")
		return
	}

	user, err := r.UserService.Create(req.Context(), input)
	if err != nil {
		writeError(w, http.StatusBadRequest, err.Error())
		return
	}

	writeJSON(w, http.StatusCreated, user)
}
