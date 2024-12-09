insert into licences(id, booking_id, prison_number, stage, version, vary_version, deleted_at, licence_in_cvl)
values
    (1, 10, 'A1234AA', 'ELIGIBILITY', '1', '0', null,false),
    (2, 20, 'A1234BB', 'ELIGIBILITY', '1', '0', null, false),
    (3, 30, 'A1234CC', 'ELIGIBILITY', '1', '0', null, false),
    (4, 40, 'A1234DD', 'ELIGIBILITY', '1', '0', '2022-07-27 15:00:00', false),
    (5, 40, 'A1234DD', 'ELIGIBILITY', '1', '0', null, false),
    (6, 50, 'A1234AA', 'ELIGIBILITY', '1', '0', null, false);
