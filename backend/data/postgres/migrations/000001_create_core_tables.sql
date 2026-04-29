-- +goose Up
CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    username VARCHAR(80) NOT NULL UNIQUE,
    email VARCHAR(160) NOT NULL UNIQUE,
    password TEXT NOT NULL,
    photo TEXT,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS projects (
    project_id SERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    manager_id INTEGER NOT NULL,
    created_by INTEGER NOT NULL,
    start_date DATE NOT NULL,
    estimated_end_date DATE NOT NULL,
    actual_end_date DATE,
    CONSTRAINT fk_projects_manager
        FOREIGN KEY (manager_id) REFERENCES users(user_id),
    CONSTRAINT fk_projects_created_by
        FOREIGN KEY (created_by) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS project_users (
    project_user_id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    CONSTRAINT fk_project_users_project
        FOREIGN KEY (project_id) REFERENCES projects(project_id),
    CONSTRAINT fk_project_users_user
        FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT uq_project_users_project_user UNIQUE (project_id, user_id)
);

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
        FOREIGN KEY (project_id) REFERENCES projects(project_id),
    CONSTRAINT fk_tasks_user
        FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE INDEX IF NOT EXISTS idx_projects_manager_id ON projects(manager_id);
CREATE INDEX IF NOT EXISTS idx_projects_created_by ON projects(created_by);
CREATE INDEX IF NOT EXISTS idx_project_users_project_id ON project_users(project_id);
CREATE INDEX IF NOT EXISTS idx_project_users_user_id ON project_users(user_id);
CREATE INDEX IF NOT EXISTS idx_tasks_project_id ON tasks(project_id);
CREATE INDEX IF NOT EXISTS idx_tasks_user_id ON tasks(user_id);

-- +goose Down
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS project_users;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS users;
