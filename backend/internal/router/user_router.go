package router

import (
	"crypto/rand"
	"encoding/hex"
	"encoding/json"
	"errors"
	"io"
	"net/http"
	"os"
	"path/filepath"
	"strconv"
	"strings"

	"commov/backend/internal/services"
	"github.com/go-chi/chi/v5"
)

type UserRouter struct {
	UserService *services.UserService
	AuthService *services.AuthService
}

const photoUploadDir = "photos/image"

func (r *UserRouter) Register(chiRouter chi.Router) {
	chiRouter.Get("/users", r.list)
	chiRouter.Post("/users", r.create)
	chiRouter.Delete("/users/{userId}", r.delete)
	chiRouter.Post("/photos/image", r.uploadPhoto)
	chiRouter.Handle("/photos/image/*", http.StripPrefix("/photos/image/", http.FileServer(http.Dir(photoUploadDir))))
}

func (r *UserRouter) list(w http.ResponseWriter, req *http.Request) {
	actor, loggedIn, err := r.AuthService.CheckLogin(req.Context(), authToken(req))
	if err != nil {
		writeUserServiceError(w, err)
		return
	}
	if !loggedIn {
		writeError(w, http.StatusUnauthorized, "invalid token")
		return
	}

	users, err := r.UserService.List(req.Context(), actor.UserID)
	if err != nil {
		writeUserServiceError(w, err)
		return
	}

	writeJSON(w, http.StatusOK, users)
}

func (r *UserRouter) create(w http.ResponseWriter, req *http.Request) {
	defer req.Body.Close()

	actor, loggedIn, err := r.AuthService.CheckLogin(req.Context(), authToken(req))
	if err != nil {
		writeUserServiceError(w, err)
		return
	}
	if !loggedIn {
		writeError(w, http.StatusUnauthorized, "invalid token")
		return
	}

	var input struct {
		Name     string `json:"name"`
		Username string `json:"username"`
		Email    string `json:"email"`
		Password string `json:"password"`
		Photo    string `json:"photo"`
		Role     string `json:"role"`
	}
	if err := json.NewDecoder(req.Body).Decode(&input); err != nil {
		writeError(w, http.StatusBadRequest, "invalid request body")
		return
	}

	trueVal := true
	user, err := r.UserService.Create(
		req.Context(),
		actor.UserID,
		input.Name,
		input.Username,
		input.Email,
		input.Password,
		input.Photo,
		input.Role,
		&trueVal,
	)
	if err != nil {
		writeUserServiceError(w, err)
		return
	}

	writeJSON(w, http.StatusCreated, user)
}

func (r *UserRouter) delete(w http.ResponseWriter, req *http.Request) {
	actor, loggedIn, err := r.AuthService.CheckLogin(req.Context(), authToken(req))
	if err != nil {
		writeUserServiceError(w, err)
		return
	}
	if !loggedIn {
		writeError(w, http.StatusUnauthorized, "invalid token")
		return
	}

	userID, err := strconv.Atoi(chi.URLParam(req, "userId"))
	if err != nil {
		writeError(w, http.StatusBadRequest, "userId is invalid")
		return
	}

	if err := r.UserService.Delete(req.Context(), actor.UserID, userID); err != nil {
		writeUserServiceError(w, err)
		return
	}

	w.WriteHeader(http.StatusNoContent)
}

func (r *UserRouter) uploadPhoto(w http.ResponseWriter, req *http.Request) {
	defer req.Body.Close()

	req.Body = http.MaxBytesReader(w, req.Body, 10<<20)
	if err := req.ParseMultipartForm(10 << 20); err != nil {
		writeError(w, http.StatusBadRequest, "invalid multipart form")
		return
	}

	file, fileHeader, err := req.FormFile("photo")
	if err != nil {
		writeError(w, http.StatusBadRequest, "photo is required")
		return
	}
	defer file.Close()

	if err := os.MkdirAll(photoUploadDir, 0755); err != nil {
		writeError(w, http.StatusInternalServerError, "failed to create upload directory")
		return
	}

	filename, err := photoFilename(fileHeader.Filename)
	if err != nil {
		writeError(w, http.StatusInternalServerError, "failed to generate filename")
		return
	}

	path := filepath.Join(photoUploadDir, filename)
	destination, err := os.Create(path)
	if err != nil {
		writeError(w, http.StatusInternalServerError, "failed to create photo")
		return
	}
	defer destination.Close()

	if _, err := io.Copy(destination, file); err != nil {
		writeError(w, http.StatusInternalServerError, "failed to save photo")
		return
	}

	writeJSON(w, http.StatusCreated, map[string]string{
		"path": filepath.ToSlash(path),
	})
}

func photoFilename(originalFilename string) (string, error) {
	randomBytes := make([]byte, 16)
	if _, err := rand.Read(randomBytes); err != nil {
		return "", err
	}

	extension := strings.ToLower(filepath.Ext(originalFilename))
	return hex.EncodeToString(randomBytes) + extension, nil
}

func writeUserServiceError(w http.ResponseWriter, err error) {
	message := cleanServiceErrorMessage(err)

	switch {
	case errors.Is(err, services.ErrUnauthorized):
		writeError(w, http.StatusUnauthorized, message)
	case errors.Is(err, services.ErrForbidden):
		writeError(w, http.StatusForbidden, message)
	case errors.Is(err, services.ErrConflict):
		writeError(w, http.StatusConflict, message)
	case errors.Is(err, services.ErrNotFound):
		writeError(w, http.StatusNotFound, message)
	case errors.Is(err, services.ErrValidation):
		writeError(w, http.StatusBadRequest, message)
	default:
		writeError(w, http.StatusInternalServerError, "internal server error")
	}
}

func cleanServiceErrorMessage(err error) string {
	message := err.Error()
	for _, prefix := range []string{
		services.ErrUnauthorized.Error() + ": ",
		services.ErrForbidden.Error() + ": ",
		services.ErrConflict.Error() + ": ",
		services.ErrNotFound.Error() + ": ",
		services.ErrValidation.Error() + ": ",
	} {
		message = strings.TrimPrefix(message, prefix)
	}

	return message
}
