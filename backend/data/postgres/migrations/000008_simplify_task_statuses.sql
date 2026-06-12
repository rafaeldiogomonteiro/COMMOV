-- +goose Up
UPDATE tasks
SET status = 'todo'
WHERE LOWER(TRIM(status)) <> 'completed';

-- +goose Down
UPDATE tasks
SET status = 'pending'
WHERE LOWER(TRIM(status)) = 'todo';
