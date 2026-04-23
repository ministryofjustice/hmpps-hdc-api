INSERT INTO licences
(
    id,
    licence,
    booking_id,
    stage,
    "version",
    transition_date,
    vary_version,
    additional_conditions_version,
    standard_conditions_version,
    prison_number,
    deleted_at,
    licence_in_cvl
)
VALUES
    (
        1,
        '{
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
            }
        }'::jsonb,
        54222,
        'ELIGIBILITY',
        1,
        '2021-08-06 15:06:37.188',
        0,
        '1',
        '2',
        'A12345B',
        NULL,
        FALSE
    );

INSERT INTO audit (timestamp, "user", action, details)
VALUES
    (
        '2021-04-05 15:06:37.188',
        'creator',
        'LICENCE_RECORD_STARTED',
        '{
          "bookingId": "54222"
        }'::jsonb
    );
