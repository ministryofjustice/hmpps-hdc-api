INSERT INTO licences (licence, booking_id, stage, "version", transition_date, vary_version, additional_conditions_version, standard_conditions_version, prison_number, deleted_at, licence_in_cvl)
VALUES
-- 1. Wrong stage
('{
  "document": { "template": { "decision": "hdc_ap" } },
  "curfew": {
    "approvedPremisesAddress": { "postCode": "A1" }
  }
}'::jsonb,
 1, 'DRAFT', 1, '2021-01-01', 0, '2', '2', 'C2001AA', null, false),
-- 2. Missing document.template
('{
  "curfew": {
    "approvedPremisesAddress": { "postCode": "A2" }
  }
}'::jsonb,
 2, 'DECIDED', 1, '2021-01-01', 0, '2', '2', 'C2001AB', null, false),
-- 3. No OR address fields at all
('{
  "document": { "template": { "decision": "hdc_ap" } }
}'::jsonb,
 3, 'DECIDED', 1, '2021-01-01', 0, '2', '2', 'C2001AC', null, false),
-- 4. curfew.approvedPremisesAddress only but missing document
('{
  "curfew": {
    "approvedPremisesAddress": { "postCode": "A4" }
  }
}'::jsonb,
 4, 'DECIDED', 1, '2021-01-01', 0, '2', '2', 'C2001AD', null, false),
-- 5. bassReferral.approvedPremisesAddress but wrong stage
('{
  "document": { "template": { "decision": "hdc_ap" } },
  "bassReferral": {
    "approvedPremisesAddress": { "postCode": "A5" }
  }
}'::jsonb,
 5, 'IN_PROGRESS', 1, '2021-01-01', 0, '2', '2', 'C2001AE', null, false),
-- 6. proposedAddress.curfewAddress but missing document
('{
  "proposedAddress": {
    "curfewAddress": "21 Test Street"
  }
}'::jsonb,
 6, 'DECIDED', 1, '2021-01-01', 0, '2', '2', 'C2001AF', null, false),
-- 7. bassOffer but missing document
('{
  "bassReferral": {
    "bassOffer": { "status": "OFFERED" }
  }
}'::jsonb,
 7, 'DECIDED', 1, '2021-01-01', 0, '2', '2', 'C2001AG', null, false),
-- 8. valid doc but no OR matches
('{
  "document": { "template": { "decision": "hdc_ap" } },
  "curfew": {
    "curfewHours": { "allFrom": "19:00" }
  }
}'::jsonb,
 8, 'DECIDED', 1, '2021-01-01', 0, '2', '2', 'C2001AH', null, false);
