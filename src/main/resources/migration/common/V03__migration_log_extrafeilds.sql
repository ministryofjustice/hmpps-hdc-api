
CREATE TYPE migration_error_source AS ENUM ('CVL', 'HDC');

ALTER TABLE licence_migration_log
    ADD COLUMN success BOOLEAN,
    ADD COLUMN retry BOOLEAN,
    ADD COLUMN message TEXT,
    ADD COLUMN error_source migration_error_source;

