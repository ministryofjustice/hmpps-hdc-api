INSERT INTO licence_versions (id,
                              template,
                              licence,
                              booking_id,
                              version,
                              vary_version,
                              prison_number,
                              deleted_at,
                              licence_in_cvl,
                              "timestamp")
-- 1. Wrong stage
VALUES (1,
        'hdc_ap',
        '{
            "document": { "template": { "decision": "hdc_ap" } },
            "curfew": {
                "approvedPremisesAddress": { "postCode": "A1" }
            }
        }'::jsonb,
        1, 1, 0, 'C2001AA', null, false, now()),
-- 2. Missing document.template
       (2,
        'hdc_ap',
        '{
            "curfew": {
                "approvedPremisesAddress": { "postCode": "A2" }
            }
        }'::jsonb,
        2, 1, 0, 'C2001AB', null, false, now()),
-- 3. No OR address fields at all
       (3,
        'hdc_ap',
        '{
            "document": { "template": { "decision": "hdc_ap" } }
        }'::jsonb,
        3, 1, 0, 'C2001AC', null, false, now()),
-- 4. curfew.approvedPremisesAddress only but missing document
       (4,
        'hdc_ap',
        '{
            "curfew": {
                "approvedPremisesAddress": { "postCode": "A4" }
            }
       }'::jsonb,
        4, 1, 0, 'C2001AD', null, false, now()),
-- 5. bassReferral.approvedPremisesAddress but wrong stage
       (5,
        'hdc_ap',
        '{
            "document": { "template": { "decision": "hdc_ap" } },
            "bassReferral": {
                "approvedPremisesAddress": { "postCode": "A5" }
            }
        }'::jsonb,
        5, 1, 0, 'C2001AE', null, false, now()),
-- 6. proposedAddress.curfewAddress but missing document
       (6,
   'hdc_ap',
        '{
           "proposedAddress": {
                "curfewAddress": {
                    "postCode": "TST1 1TS",
                    "addressTown": "addressTown",
                    "addressLine1": "addressLine1",
                    "addressLine2": "addressLine2"
                }
            }
        }'::jsonb,
        6, 1, 0, 'C2001AF', null, false, now()),
-- 7. bassOffer but missing document
       (7,
        'hdc_ap',
        '{
            "bassReferral": {
                "bassOffer": { "status": "OFFERED" }
            }
        }'::jsonb,
        7, 1, 0, 'C2001AG', null, false, now()),
-- 8. valid doc but no OR matches
       (8,
        'hdc_ap',
        '{
            "document": { "template": { "decision": "hdc_ap" } },
            "curfew": {
                "curfewHours": { "allFrom": "19:00" }
            }
        }'::jsonb,
        8, 1, 0, 'C2001AH', null, false, now()),
-- 9. has been deleted
       (9,
        'hdc_ap',
        '{
            "document": { "template": { "decision": "hdc_ap" } },
        "curfew": {
            "approvedPremisesAddress": { "postCode": "A1" }
        }
        }'::jsonb,
        9, 1, 0, 'C2001AH', now(), false, now());

