INSERT INTO licence_migration_log (
    booking_id,
    prison_number,
    success,
    retry,
    message
)
VALUES
    (54222, 'A1234AA', false, false, 'First failure'),
    (54222, 'A1234AA', false, false, 'Second failure'),
    (54222, 'A1234AA', false, false, 'Third failure'),
    -- Only one failure, so should not be returned
    (54321, 'B1234BB', false, false, 'Failure'),
    -- Has a successful migration, so should not be returned
    (55555, 'C1234CC', false, false, 'First failure'),
    (55555, 'C1234CC', true, false, 'Migration succeeded'),
    (55555, 'C1234CC', false, false, 'Another failure');