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
          "reporting": {
            "reportingInstructions": {
              "name": "Officer Person",
              "postcode": "TT1 1TT",
              "telephone": "000000000000",
              "townOrCity": "Test Town",
              "organisation": "Test Organisation"
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
SELECT NOW(),
       'test-user',
       'LICENCE_RECORD_STARTED',
       jsonb_build_object('bookingId', booking_id::text)
    FROM licence_versions;

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

