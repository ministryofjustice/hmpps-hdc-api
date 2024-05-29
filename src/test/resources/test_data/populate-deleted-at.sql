insert into licences(id, booking_id, prison_number, stage, version, vary_version, deleted_at)
values
    (1, 10, 'A1234AA', 'ELIGIBILITY', '1', '0', null),
    (2, 20, 'A1234BB', 'ELIGIBILITY', '1', '0', null),
    (3, 30, 'A1234CC', 'ELIGIBILITY', '1', '0', null),
    (4, 40, 'A1234DD', 'ELIGIBILITY', '1', '0', '2022-07-27 15:00:00'),
    (5, 50, 'A1234EE', 'ELIGIBILITY', '1', '0', null),
    (6, 60, 'A1234FF', 'ELIGIBILITY', '1', '0', null);

insert into licence_versions(id, booking_id, prison_number, version, vary_version, template,  deleted_at)
values
    (11, 10, 'A1234AA', '2', '0', 'hdc_ap', null),
    (12, 20, 'A1234BB', '1', '0', 'hdc_ap', null),
    (13, 30, 'A1234CC', '1', '0', 'hdc_ap', null),
    (14, 30, 'A1234CC', '2', '0', 'hdc_ap', null),
    (15, 40, 'A1234DD', '1', '0', 'hdc_ap', '2022-07-27 15:00:00'),
    (16, 50, 'A1234AA', '1', '0', 'hdc_ap', '2022-07-27 15:00:00'),
    (17, 60, 'A1234FF', '1', '0', 'hdc_ap', null),
    (18, 70, 'A1234EE', '1', '0', 'hdc_ap', null);
