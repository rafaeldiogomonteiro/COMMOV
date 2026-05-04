-- +goose Up
CREATE TABLE IF NOT EXISTS tasks (
    task_id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    title VARCHAR(160) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    estimated_end_date DATE,
    actual_end_date DATE,
    estimated_time DOUBLE PRECISION NOT NULL DEFAULT 0,
    time_spent DOUBLE PRECISION NOT NULL DEFAULT 0,
    completion_rate DOUBLE PRECISION NOT NULL DEFAULT 0,
    work_date DATE,
    location VARCHAR(160),
    observation TEXT,
    photo TEXT,
    CONSTRAINT fk_tasks_project
        FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE,
    CONSTRAINT fk_tasks_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- +goose Down
DROP TABLE IF EXISTS tasks;
