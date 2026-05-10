-- +goose Up
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

-- +goose Down
DROP TABLE IF EXISTS projects;
