package main

import (
	"log"
	"net/http"
	"os"

	"commov/backend/internal/config"
	"commov/backend/internal/postgres"
	"commov/backend/internal/router"
	"commov/backend/internal/services"
)

func main() {
	cfg := config.Load()

	appLogger := log.Default()

	db, err := postgres.NewDB(cfg.DatabaseURL)
	if err != nil {
		appLogger.Printf("failed to connect database: %v", err)
		os.Exit(1)
	}

	userRepo := &postgres.UserRepo{DB: db}
	userService := &services.UserService{UserRepo: userRepo}
	userRouter := &router.UserRouter{UserService: userService}

	appRouter := router.NewRouter(router.Dependencies{
		Logger:     appLogger,
		UserRouter: userRouter,
	})

	appLogger.Printf("server listening on %s", cfg.HTTPAddr)
	if err := http.ListenAndServe(cfg.HTTPAddr, appRouter); err != nil {
		appLogger.Printf("server failed: %v", err)
		os.Exit(1)
	}
}
