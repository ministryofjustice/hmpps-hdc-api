-- License 1 → the latest retry false no processing - No Migration
INSERT INTO licence_versions (
    id,
    template,
    licence,
    booking_id,
    version,
    vary_version,
    prison_number,
    deleted_at,
    licence_in_cvl,
    "timestamp")
VALUES (
        1,
        'hdc_ap',
        '{
          "document": {
            "template": {
              "decision": "hdc_ap",
              "offenceCommittedBeforeFeb2015": "No"
            }
          },
          "reporting": {
            "reportingInstructions": {
              "name": "Officer Person",
              "postcode": "TT1 1TT",
              "telephone": "000000000000",
              "townOrCity": "Test Town",
              "organisation": "Test Organisation",
              "reportingDate": "12/09/2025",
              "reportingTime": "21:00",
              "buildingAndStreet1": "Test Street",
              "buildingAndStreet2": ""
            }
          },
          "curfew": {
            "firstNight": {
              "firstNightFrom": "15:00",
              "firstNightUntil": "07:00"
            },
            "curfewHours": {
              "allFrom": "19:00",
              "allUntil": "07:00",
              "daySpecificInputs": "No"
            }
          },
          "victim": {
            "victimLiaison": {
              "decision": "Yes",
              "victimLiaisonDetails": "gghhgghh"
            }
          },
          "approval": {
            "release": {
              "decision": "Yes",
              "decisionMaker": "Decision Maker",
              "reasonForDecision": "fgtfhgfgfhgfhg"
            }
          },
          "eligibility": {
            "crdTime": { "decision": "No" },
            "excluded": { "decision": "No" },
            "suitability": { "decision": "No" }
          },
          "finalChecks": {
            "onRemand": { "decision": "No" },
            "segregation": { "decision": "No" },
            "seriousOffence": { "decision": "No" }
          },
          "bassReferral": {
            "bassRequest": {
              "bassRequested": "Yes"
            },
            "approvedPremisesAddress": {
              "postCode": "TT1 1TT",
              "addressTown": "Test Town",
              "addressLine1": "Test Street"
            }
          },
          "licenceConditions": {
            "bespoke": [
              {
                "text": "Some other condition",
                "approved": "Yes"
              }
            ],
            "standard": {
              "additionalConditionsRequired": "Yes"
            },
            "additional": {
              "REPORT_TO": {
                "reportingTime": "12:30",
                "reportingAddress": "Sheffield Police Station"
              },
              "REMAIN_ADDRESS": {
                "curfewTo": "16:30",
                "curfewFrom": "12:30",
                "curfewAddress": "21 Smith Street"
              },
              "ALCOHOL_MONITORING": {
                "endDate": "12/11/2026",
                "timeframe": "4 weeks"
              }
            }
          }
        }'::jsonb,
        10,
        1,
        0,
        'A1234EE',
        null,
        false,
        NOW());

-- LICENCE 2 failed + retry=true -> SHOULD RETURN
INSERT INTO licence_versions (
    id,
    template,
    licence,
    booking_id,
    version,
    vary_version,
    prison_number,
    deleted_at,
    licence_in_cvl,
    "timestamp"
)
    SELECT 2,
           template,
           licence,
           11,
           version,
           vary_version+1,
           'A1234EF',
           deleted_at,
           licence_in_cvl,
           "timestamp"
    FROM licence_versions WHERE id = 1;

-- License 3 success=true -> SHOULD NOT RETURN
INSERT INTO licence_versions (
    id,
    template,
    licence,
    booking_id,
    version,
    vary_version,
    prison_number,
    deleted_at,
    licence_in_cvl
    ,"timestamp")

SELECT 3,
       template,
       licence,
       14,
       version,
       vary_version+1,
       'A1234EF',
       deleted_at,
       licence_in_cvl,
       "timestamp"
    FROM licence_versions WHERE id = 1;

--  License 4 retry=false -> SHOULD NOT RETURN
INSERT INTO licence_versions (
    id,
    template,
    licence,
    booking_id,
    version,
    vary_version,
    prison_number,
    deleted_at,
    licence_in_cvl,
    "timestamp"
)
SELECT 4,
       template,
       licence,
       15,
       version,
       vary_version,
       'A1234EH',
       deleted_at,
       licence_in_cvl,
       "timestamp"
    FROM licence_versions WHERE id = 3;

-- License 5, latest success wins -> SHOULD NOT RETURN
INSERT INTO licence_versions (
    id,
    template,
    licence,
    booking_id,
    version,
    vary_version,
    prison_number,
    deleted_at,
    licence_in_cvl,
    "timestamp"
)
SELECT 5,
       template,
       licence,
       16,
       version,
       vary_version,
       'A1234EI',
       deleted_at,
       licence_in_cvl,
       "timestamp"
    FROM licence_versions WHERE id = 3;

-- AUDIT creates min records for each licence
INSERT INTO audit (timestamp, "user", action, details)
SELECT
    a.timestamp,
    a."user" || '-' || lv.idx,
    a.action,
    jsonb_set(a.details, '{bookingId}', to_jsonb(lv.booking_id::text))
FROM (
         VALUES
             ('2021-04-05 15:06:37.188'::timestamp, 'creator',      'LICENCE_RECORD_STARTED', '{"bookingId":"10"}'::jsonb),
             ('2021-04-05 15:07:37.188'::timestamp, 'aCaseManager', 'SEND',                   '{"bookingId":"10","transitionType":"caToRo"}'::jsonb),
             ('2021-08-06 15:04:37.188'::timestamp, 'creator',      'RESET',                  '{"bookingId":"10"}'::jsonb),
             ('2021-08-06 15:05:37.188'::timestamp, 'creator',      'LICENCE_RECORD_STARTED', '{"bookingId":"10"}'::jsonb),
             ('2021-08-06 15:06:37.188'::timestamp, 'aCaseManager', 'SEND',                   '{"bookingId":"10","transitionType":"caToRo"}'::jsonb),
             ('2021-08-06 15:20:37.188'::timestamp, 'creator',      'UPDATE_SECTION',         '{"bookingId":"10","transitionType":"caToRo"}'::jsonb),
             ('2021-08-08 15:06:37.188'::timestamp, 'submitter',    'SEND',                   '{"bookingId":"10","transitionType":"roToCa"}'::jsonb),
             ('2021-08-09 15:06:37.188'::timestamp, 'updater',      'UPDATE_SECTION',         '{"bookingId":"10","transitionType":"caToRo"}'::jsonb),
             ('2021-08-10 15:06:37.188'::timestamp, 'approver',     'SEND',                   '{"bookingId":"10","transitionType":"dmToCa"}'::jsonb)
     ) AS a(timestamp, "user", action, details)
         CROSS JOIN (
    SELECT
        booking_id,
        ROW_NUMBER() OVER (ORDER BY booking_id) AS idx
    FROM licence_versions
) lv;

-- MIGRATION LOG - main test cases
INSERT INTO licence_migration_log (licence_version_id, booking_id, success, retry, message, error_source)
VALUES
    -- License Version 1 → latest retry false no processing - No Migration
    (1, 11,false, true, 'first retry','CVL'),
    (1, 11,false, false, 'latest retry failure','CVL'),
    -- License Version 2, it is a retry, - Migrate
    (2, 11, false, true, 'retryable failure','CVL'),
    -- License Version 3, it is a success, - No Migration
    (3, 14,true, false, 'migrated successfully','CVL'),
    -- License Version 4, succes=false, retry=false, - No Migration
    (4, 15,false, false, 'permanent failure','CVL'),
     -- License Version 5 → latest success wins, - No Migration
    (5,16, false, true, 'older retry failure','CVL'),
    (5,16, true, false, 'migrated successfully','CVL');

