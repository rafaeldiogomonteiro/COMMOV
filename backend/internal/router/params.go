package router

import (
	"net/http"
	"strconv"

	"github.com/go-chi/chi/v5"
)

func intURLParam(req *http.Request, name string) (int, error) {
	return strconv.Atoi(chi.URLParam(req, name))
}
