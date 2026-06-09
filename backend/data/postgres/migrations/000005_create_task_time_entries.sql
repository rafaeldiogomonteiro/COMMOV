-- +goose Up
CREATE TABLE IF NOT EXISTS task_time_entries (
    entry_id SERIAL PRIMARY KEY,
    task_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    time_spent DOUBLE PRECISION NOT NULL,
    work_date DATE NOT NULL,
    observation TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_task_time_entries_task
        FOREIGN KEY (task_id) REFERENCES tasks(task_id) ON DELETE CASCADE,
    CONSTRAINT fk_task_time_entries_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_task_time_entries_time_spent
        CHECK (time_spent > 0)
);

CREATE INDEX IF NOT EXISTS idx_task_time_entries_task_id_created_at
    ON task_time_entries (task_id, created_at DESC);

-- +goose Down
DROP TABLE IF EXISTS task_time_entries;
