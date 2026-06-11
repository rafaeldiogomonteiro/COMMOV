-- +goose Up
CREATE TABLE IF NOT EXISTS task_users (
    task_user_id SERIAL PRIMARY KEY,
    task_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    CONSTRAINT fk_task_users_task
        FOREIGN KEY (task_id) REFERENCES tasks(task_id) ON DELETE CASCADE,
    CONSTRAINT fk_task_users_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT uq_task_users_task_user UNIQUE (task_id, user_id)
);

INSERT INTO task_users (task_id, user_id)
SELECT task_id, user_id
FROM tasks
WHERE user_id > 0;

ALTER TABLE tasks DROP COLUMN user_id;

-- +goose Down
ALTER TABLE tasks ADD COLUMN user_id INTEGER;

UPDATE tasks t
SET user_id = tu.user_id
FROM (
    SELECT DISTINCT ON (task_id) task_id, user_id
    FROM task_users
    ORDER BY task_id, task_user_id
) tu
WHERE t.task_id = tu.task_id;

UPDATE tasks SET user_id = 1 WHERE user_id IS NULL;

ALTER TABLE tasks ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE tasks
    ADD CONSTRAINT fk_tasks_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;

DROP TABLE IF EXISTS task_users;
