CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    icon VARCHAR(100),
    color VARCHAR(20),
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_categories_user_id ON categories(user_id);
