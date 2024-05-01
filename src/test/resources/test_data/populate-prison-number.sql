insert into licences(id, booking_id, prison_number, stage, version, vary_version, deleted_at)
values
    (1, 10, '???', 'ELIGIBILITY', '1', '0', null),
    (2, 20, 'A1234AA', 'ELIGIBILITY', '1', '0', null),
    (3, 30, '???', 'ELIGIBILITY', '1', '0', null),
    (4, 40, '???', 'ELIGIBILITY', '1', '0', null),
    (5, 50, '???', 'ELIGIBILITY', '1', '0', null);

insert into licence_versions(id, booking_id, prison_number, version, vary_version, template, deleted_at)
values
    (11, 10, '???', '1', '0', 'hdc_ap', null),
    (12, 20, 'A1234AA', '1', '0', 'hdc_ap', null),
    (13, 30, '???', '1', '0', 'hdc_ap', null),
    (14, 40, '???', '1', '0', 'hdc_ap', null),
    (15, 50, '???', '1', '0', 'hdc_ap', null);
