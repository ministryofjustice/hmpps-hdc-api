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

VALUES (1,
        'hdc_ap',
        '{
                  "document": {
                    "template": {
                      "decision": "hdc_ap",
                      "offenceCommittedBeforeFeb2015": "No"
                    }
                  },
                  "curfew": {
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
                  "bassReferral": {
                    "bassRequest": {
                      "proposedTown": "sdfdsds",
                      "specificArea": "Yes",
                      "bassRequested": "Yes",
                      "proposedCounty": "dsdsdssd",
                      "additionalInformation": "dcsfdsf"
                    },
                    "bassAreaCheck": {
                      "bassAreaReason": "",
                      "bassAreaCheckSeen": "true",
                      "approvedPremisesRequiredYesNo": "Yes"
                    },
                    "approvedPremisesAddress": {
                      "postCode": "TT1 1TT",
                      "telephone": "00000000066",
                      "addressTown": "Test Town",
                      "addressLine1": "Test Street",
                      "addressLine2": ""
                    }
                  },
                  "bassRejections": [
                    {
                      "bassRequest": {
                        "proposedTown": "dasdsa",
                        "specificArea": "Yes",
                        "bassRequested": "Yes",
                        "proposedCounty": "dsdasd",
                        "additionalInformation": "sadfsdfsdfs"
                      },
                      "bassAreaCheck": {
                        "bassAreaReason": "sdadasd",
                        "bassAreaSuitable": "No",
                        "bassAreaCheckSeen": "true",
                        "approvedPremisesRequiredYesNo": "No"
                      },
                      "rejectionReason": "area"
                    }
                  ],
                  "licenceConditions": {
                    "bespoke": [{"text": "Some other condition", "approved": "Yes"}],
                    "standard": {
                        "additionalConditionsRequired": "Yes"
                     }
                   }
                }'::jsonb,
        54222,
        1,
        0,
        'A12345B',
        NULL,
        FALSE,
        NOW());

    INSERT INTO licence_versions (id, template, licence, booking_id, version, vary_version, prison_number, deleted_at, licence_in_cvl, "timestamp")
    SELECT
        v.id,
        l.template,
        l.licence,
        l.booking_id,
        v.version,
        v.vary_version,
        l.prison_number,
        l.deleted_at,
        l.licence_in_cvl,
        NOW()
    FROM licence_versions l CROSS JOIN (
        VALUES
            (2, 1, 4),
            (3, 2, 1),
            (4, 1, 1)
    ) AS v(id, version, vary_version) WHERE l.id = 1;


-- audit data

INSERT INTO audit (timestamp, "user", action, details)
VALUES ('2021-04-05 15:06:37.188',
        'creator',
        'LICENCE_RECORD_STARTED',
        '{
          "bookingId": "98765"
        }'::jsonb),
       ('2021-04-05 15:06:37.188',
        'creator',
        'LICENCE_RECORD_STARTED',
        '{
          "bookingId": "54222"
        }'::jsonb),
       ('2021-04-05 15:07:37.188',
        'aCaseManager',
        'SEND',
        '{
          "bookingId": "54222",
          "transitionType": "caToRo"
        }'::jsonb),
       ('2021-08-06 15:04:37.188',
        'creator',
        'RESET',
        '{
          "bookingId": "54222"
        }'::jsonb),
       ('2021-08-06 15:05:37.188',
        'creator',
        'LICENCE_RECORD_STARTED',
        '{
          "bookingId": "54222"
        }'::jsonb),
       ('2021-08-06 15:06:37.188',
        'aCaseManager',
        'SEND',
        '{
          "bookingId": "54222",
          "transitionType": "caToRo"
        }'::jsonb),
       ('2021-08-06 15:20:37.188',
        'creator',
        'UPDATE_SECTION',
        '{
          "bookingId": "54222",
          "transitionType": "caToRo"
        }'::jsonb),
       ('2021-08-08 15:06:37.188',
        'submitter',
        'SEND',
        '{
          "bookingId": "54222",
          "transitionType": "roToCa"
        }'::jsonb),
       ('2021-08-09 15:06:37.188',
        'updater',
        'UPDATE_SECTION',
        '{
          "bookingId": "54222",
          "transitionType": "caToRo"
        }'::jsonb),
       ('2021-08-10 15:06:37.188',
        'approver',
        'SEND',
        '{
          "bookingId": "54222",
          "transitionType": "dmToCa"
        }'::jsonb);
