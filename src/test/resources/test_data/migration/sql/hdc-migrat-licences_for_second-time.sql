-- LICENCE 1 Full details No migration log -> SHOULD RETURN
INSERT INTO licences (id,
                      licence,
                      booking_id,
                      stage,
                      version,
                      transition_date,
                      vary_version,
                      additional_conditions_version,
                      standard_conditions_version,
                      prison_number,
                      deleted_at,
                      licence_in_cvl)
VALUES (1,
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
        'DECIDED',
        1,
        '2021-08-06 15:06:37.188',
        0,
        '2',
        '2',
        'A1234EE',
        null,
        false);

-- LICENCE 2 failed + retry=true -> SHOULD RETURN
INSERT INTO licences (id,
                      licence,
                      booking_id,
                      stage,
                      version,
                      transition_date,
                      vary_version,
                      additional_conditions_version,
                      standard_conditions_version,
                      prison_number,
                      deleted_at,
                      licence_in_cvl)
SELECT 2,
       licence,
       11,
       stage,
       version,
       transition_date,
       vary_version,
       additional_conditions_version,
       standard_conditions_version,
       'A1234EF',
       deleted_at,
       licence_in_cvl
FROM licences
WHERE id = 1;

-- License 3 success=true -> SHOULD NOT RETURN
INSERT INTO licences (id,
                      licence,
                      booking_id,
                      stage,
                      version,
                      transition_date,
                      vary_version,
                      additional_conditions_version,
                      standard_conditions_version,
                      prison_number,
                      deleted_at,
                      licence_in_cvl)
SELECT 3,
       licence,
       11,
       stage,
       version,
       transition_date,
       vary_version,
       additional_conditions_version,
       standard_conditions_version,
       'A1234EF',
       deleted_at,
       licence_in_cvl
FROM licences
WHERE id = 1;

--  License 4 retry=false -> SHOULD NOT RETURN
INSERT INTO licences
SELECT 4,
       licence,
       13,
       stage,
       version,
       transition_date,
       vary_version,
       additional_conditions_version,
       standard_conditions_version,
       'A1234EH',
       deleted_at,
       licence_in_cvl
FROM licences
WHERE id = 3;

-- License 5, latest success wins -> SHOULD NOT RETURN
INSERT INTO licences
SELECT 5,
       licence,
       14,
       stage,
       version,
       transition_date,
       vary_version,
       additional_conditions_version,
       standard_conditions_version,
       'A1234EI',
       deleted_at,
       licence_in_cvl
FROM licences
WHERE id = 3;

-- AUDIT creates min records for each licence
INSERT INTO audit (timestamp, "user", action, details)
SELECT NOW(),
       'test-user',
       'LICENCE_RECORD_STARTED',
       jsonb_build_object('bookingId', booking_id::text)
FROM licences;

-- MIGRATION LOG - main test cases
INSERT INTO licence_migration_log (licence_id, success, retry, message, error_source)
VALUES
    -- License 1 → latest retry false no processing - No Migration
    (1, false, true, 'first retry','CVL'),
    (1, false, false, 'latest retry failure','CVL'),
    -- License 2, it is a retry, - Migrate
    (2, false, true, 'retryable failure','CVL'),
    -- License 3, it is a success, - No Migration
    (3, true, false, 'migrated successfully','CVL'),
    -- License 4, succes=false, retry=false, - No Migration
    (4, false, false, 'permanent failure','CVL'),
     -- License 5 → latest success wins, - No Migration
    (5, false, true, 'older retry failure','CVL'),
    (5, true, false, 'migrated successfully','CVL');

