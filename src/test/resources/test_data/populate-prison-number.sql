insert into licences(id, booking_id, prison_number, stage, version, vary_version)
values
    (1, 10, '???', 'ELIGIBILITY', '1', '0'),
    (2, 20, 'A1234AA', 'ELIGIBILITY', '1', '0'),
    (3, 30, '???', 'ELIGIBILITY', '1', '0'),
    (4, 40, '???', 'ELIGIBILITY', '1', '0'),
    (5, 50, '???', 'ELIGIBILITY', '1', '0');

insert into licence_versions(id, booking_id, prison_number, version, vary_version, template)
values
    (11, 10, '???', '1', '0', 'hdc_ap'),
    (12, 20, 'A1234AA', '1', '0', 'hdc_ap'),
    (13, 30, '???', '1', '0', 'hdc_ap'),
    (14, 40, '???', '1', '0', 'hdc_ap'),
    (15, 50, '???', '1', '0', 'hdc_ap');
