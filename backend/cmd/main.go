package main

import (
	"context"
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
	authService := &services.AuthService{UserRepo: userRepo}
	userService := &services.UserService{UserRepo: userRepo}
	if _, created, err := userService.EnsureDefaultUser(context.Background(), cfg.DefaultUser, cfg.DefaultUserPass); err != nil {
		appLogger.Printf("failed to ensure default user: %v", err)
		os.Exit(1)
	} else if created {
		appLogger.Printf("default user created: %s", cfg.DefaultUser)
	}

	authRouter := &router.AuthRouter{AuthService: authService}
	userRouter := &router.UserRouter{UserService: userService, AuthService: authService}

	appRouter := router.NewRouter(router.Dependencies{
		Logger:     appLogger,
		AuthRouter: authRouter,
		UserRouter: userRouter,
	})

	appLogger.Printf("server listening on %s", cfg.HTTPAddr)
	if err := http.ListenAndServe(cfg.HTTPAddr, appRouter); err != nil {
		appLogger.Printf("server failed: %v", err)
		os.Exit(1)
	}
}
