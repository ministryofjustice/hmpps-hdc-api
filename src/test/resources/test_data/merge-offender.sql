insert into licences(id, booking_id, prison_number, stage, version, vary_version)
values
    (1, 10, 'A1234AA', 'ELIGIBILITY', '1', '0'),
    (2, 20, 'A1234AA', 'ELIGIBILITY', '1', '0'),
    (3, 30, null, 'ELIGIBILITY', '1', '0'),
    (4, 40, 'A1234AA', 'ELIGIBILITY', '1', '0'),
    (5, 50, 'C1234CC', 'ELIGIBILITY', '1', '0'),
    (6, 60, 'C1234CC', 'ELIGIBILITY', '1', '0'),
    (7, 70, 'B1234BB', 'ELIGIBILITY', '1', '0');


insert into licence_versions(id, booking_id, prison_number, version, vary_version, template)
values
    (11, 10, 'A1234AA', '1', '0', 'hdc_ap'),
    (13, 30, null, '1', '0', 'hdc_ap'),
    (15, 50, 'C1234CC', '1', '0', 'hdc_ap'),
    (17, 70, 'B1234BB', '1', '0', 'hdc_ap');
