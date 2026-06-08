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
	projectRepo := &postgres.ProjectRepo{DB: db}
	projectUserRepo := &postgres.ProjectUserRepo{DB: db}
	taskRepo := &postgres.TaskRepo{DB: db}
	authService := &services.AuthService{UserRepo: userRepo}
	userService := &services.UserService{
		UserRepo:    userRepo,
		ProjectRepo: projectRepo,
		AuthService: authService,
	}
	projectService := &services.ProjectService{
		ProjectRepo:     projectRepo,
		ProjectUserRepo: projectUserRepo,
		UserRepo:        userRepo,
	}
	taskService := &services.TaskService{
		TaskRepo:        taskRepo,
		ProjectRepo:     projectRepo,
		ProjectUserRepo: projectUserRepo,
		UserRepo:        userRepo,
	}
	if _, created, err := userService.EnsureDefaultUser(context.Background(), cfg.DefaultUser, cfg.DefaultUserPass); err != nil {
		appLogger.Printf("failed to ensure default user: %v", err)
		os.Exit(1)
	} else if created {
		appLogger.Printf("default user created: %s", cfg.DefaultUser)
	}

	authRouter := &router.AuthRouter{AuthService: authService, UserService: userService}
	userRouter := &router.UserRouter{UserService: userService, AuthService: authService}
	projectRouter := &router.ProjectRouter{ProjectService: projectService, AuthService: authService}
	taskRouter := &router.TaskRouter{TaskService: taskService, AuthService: authService}

	appRouter := router.NewRouter(router.Dependencies{
		Logger:        appLogger,
		AuthRouter:    authRouter,
		UserRouter:    userRouter,
		ProjectRouter: projectRouter,
		TaskRouter:    taskRouter,
	})

	appLogger.Printf("server listening on %s", cfg.HTTPAddr)
	if err := http.ListenAndServe(cfg.HTTPAddr, appRouter); err != nil {
		appLogger.Printf("server failed: %v", err)
		os.Exit(1)
	}
}
