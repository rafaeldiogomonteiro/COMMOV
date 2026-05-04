-- +goose Up
CREATE TABLE IF NOT EXISTS project_users (
    project_user_id SERIAL PRIMARY KEY,
    project_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    CONSTRAINT fk_project_users_project
        FOREIGN KEY (project_id) REFERENCES projects(project_id) ON DELETE CASCADE,
    CONSTRAINT fk_project_users_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT uq_project_users_project_user UNIQUE (project_id, user_id)
);

-- +goose Down
DROP TABLE IF EXISTS project_users;
