INSERT INTO licence_migration_log(licence_version_id, booking_id, success, retry, message, error_source)
VALUES (1, 10, true, false, 'migrated successfully', NULL),
       (2, 20, false, true, 'Service has failed - retry', CAST('CVL' AS migration_error_source)),
       (3, 30, false, false, 'Prisoner not found for prisoner number C1234EE', CAST('HDC' AS migration_error_source));
