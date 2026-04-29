DOCKER_COMPOSE ?= docker compose
DATABASE_URL ?= postgres://postgres:postgres@localhost:5432/commov?sslmode=disable
MIGRATIONS_DIR ?= data/postgres/migrations
GOOSE ?= go run github.com/pressly/goose/v3/cmd/goose@latest

.PHONY: start stop delete up down goose-up goose-down

start:
	$(DOCKER_COMPOSE) up -d --build

stop:
	$(DOCKER_COMPOSE) down

delete:
	$(DOCKER_COMPOSE) down -v --remove-orphans

up: goose-up

down: goose-down

goose-up:
	$(GOOSE) -dir $(MIGRATIONS_DIR) postgres "$(DATABASE_URL)" up

goose-down:
	$(GOOSE) -dir $(MIGRATIONS_DIR) postgres "$(DATABASE_URL)" down
