CREATE TABLE licence_migration_log (
       id BIGSERIAL PRIMARY KEY,
       licence_id BIGINT NOT NULL,
       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_licence_migration_log_licence_id
    ON licence_migration_log (licence_id);
