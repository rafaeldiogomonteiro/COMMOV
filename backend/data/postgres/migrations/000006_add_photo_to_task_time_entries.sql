-- +goose Up
ALTER TABLE task_time_entries
    ADD COLUMN IF NOT EXISTS photo TEXT NOT NULL DEFAULT '';

-- +goose Down
ALTER TABLE task_time_entries
    DROP COLUMN IF EXISTS photo;
