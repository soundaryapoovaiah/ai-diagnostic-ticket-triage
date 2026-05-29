CREATE TABLE app_users (
                           id BIGSERIAL PRIMARY KEY,
                           full_name VARCHAR(150) NOT NULL,
                           email VARCHAR(150) NOT NULL UNIQUE,
                           password_hash VARCHAR(255) NOT NULL,
                           role VARCHAR(50) NOT NULL,
                           enabled BOOLEAN NOT NULL DEFAULT TRUE,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP
);

CREATE INDEX idx_app_users_email ON app_users(email);
CREATE INDEX idx_app_users_role ON app_users(role);