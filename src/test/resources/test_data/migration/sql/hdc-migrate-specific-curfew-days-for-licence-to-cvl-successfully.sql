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
VALUES
    (
        1,
        'hdc_ap',
'{
          "document": {
            "template": {
              "decision": "hdc_ap",
              "offenceCommittedBeforeFeb2015": "No"
            }
          },
            "proposedAddress": {
                "curfewAddress": {
                    "postCode": "TST1 1TS",
                    "addressTown": "addressTown",
                    "addressLine1": "addressLine1",
                    "addressLine2": "addressLine2"
                }
            },
            "curfew": {
                "curfewHours": {
                    "fridayFrom": "10:00",
                    "mondayFrom": "11:00",
                    "sundayFrom": "12:00",
                    "fridayUntil": "03:00",
                    "mondayUntil": "04:00",
                    "sundayUntil": "05:00",
                    "tuesdayFrom": "13:00",
                    "saturdayFrom": "14:00",
                    "thursdayFrom": "15:00",
                    "tuesdayUntil": "06:00",
                    "saturdayUntil": "07:00",
                    "thursdayUntil": "08:00",
                    "wednesdayFrom": "09:00",
                    "wednesdayUntil": "16:00",
                    "daySpecificInputs": "Yes"
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
                },
                "consideration": {
                    "decision": "Yes"
                }
            },
            "document": {
                "template": {
                    "decision": "hdc_ap",
                    "offenceCommittedBeforeFeb2015": "No"
                }
            },
            "eligibility": {
                "crdTime": {
                    "decision": "No"
                },
                "excluded": {
                    "decision": "No"
                },
                "suitability": {
                    "decision": "No"
                }
            },
            "finalChecks": {
                "onRemand": {
                    "decision": "No"
                },
                "segregation": {
                    "decision": "No"
                },
                "seriousOffence": {
                    "decision": "No"
                },
                "confiscationOrder": {
                    "decision": "No"
                },
                "undulyLenientSentence": {
                    "decision": "No"
                }
            },
          "licenceConditions": {
            "bespoke": [{"text": "Some other condition", "approved": "Yes"}],
            "standard": {
                "additionalConditionsRequired": "Yes"
             },
            "additional": {
               "POLYGRAPH": {}
            }
           }
        }'::jsonb,
        54222,
        1,
        0,
        'A1234EE',
        null,
        false,
        NOW());

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
