package router

import (
	"log"
	"net/http"

	"github.com/go-chi/chi/v5"
)

type Dependencies struct {
	Logger            *log.Logger
	AuthRouter        *AuthRouter
	UserRouter        *UserRouter
	ProjectRouter     *ProjectRouter
	TaskRouter        *TaskRouter
	StatisticsRouter  *StatisticsRouter
}

func NewRouter(deps Dependencies) http.Handler {
	r := chi.NewRouter()

	r.Get("/health", func(w http.ResponseWriter, r *http.Request) {
		writeJSON(w, http.StatusOK, map[string]string{"status": "ok"})
	})

	deps.AuthRouter.Register(r)
	deps.UserRouter.Register(r)
	deps.ProjectRouter.Register(r)
	deps.TaskRouter.Register(r)
	deps.StatisticsRouter.Register(r)

	return requestLogger(deps.Logger, r)
}
