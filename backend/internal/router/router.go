package router

import (
	"log"
	"net/http"

	"github.com/go-chi/chi/v5"
)

type Dependencies struct {
	Logger     *log.Logger
	UserRouter *UserRouter
}

func NewRouter(deps Dependencies) http.Handler {
	r := chi.NewRouter()

	r.Get("/health", func(w http.ResponseWriter, r *http.Request) {
		writeJSON(w, http.StatusOK, map[string]string{"status": "ok"})
	})

	deps.UserRouter.Register(r)

	return requestLogger(deps.Logger, r)
}
