INSERT INTO licences
(id, licence, booking_id, stage, "version", transition_date, vary_version, additional_conditions_version,
 standard_conditions_version, prison_number, deleted_at, licence_in_cvl)

-- Create the data for a HDC licence with a preferred address
VALUES (1,
        '{
                  "curfew": {
                    "curfewHours": {
                      "allFrom": "19:00",
                      "allUntil": "07:00",
                      "fridayFrom": "19:00",
                      "mondayFrom": "19:00",
                      "sundayFrom": "19:00",
                      "fridayUntil": "07:00",
                      "mondayUntil": "07:00",
                      "sundayUntil": "07:00",
                      "tuesdayFrom": "19:00",
                      "saturdayFrom": "19:00",
                      "thursdayFrom": "19:00",
                      "tuesdayUntil": "07:00",
                      "saturdayUntil": "07:00",
                      "thursdayUntil": "07:00",
                      "wednesdayFrom": "19:00",
                      "wednesdayUntil": "07:00",
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
                      "name": "aaaaaa",
                      "postcode": "YO15 4AA",
                      "telephone": "000000000000",
                      "townOrCity": "aaaaa",
                      "organisation": "sadadas",
                      "reportingDate": "12/09/2025",
                      "reportingTime": "21:00",
                      "buildingAndStreet1": "12 Mayfield",
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
                      "postCode": "YO15 3LE",
                      "telephone": "00000000066",
                      "addressTown": "aaaaa",
                      "addressLine1": "12 Mayfield",
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
                  "proposedAddress": {
                    "optOut": {
                      "decision": "No"
                    },
                    "addressProposed": {
                      "decision": "No"
                    }
                  },
                  "licenceConditions": {
                    "standard": {
                      "additionalConditionsRequired": "No"
                    }
                  }
                }'::jsonb,
        54222, 'ELIGIBILITY', 1, '2021-08-06 15:06:37.188', 0, '1', '2', 'A12345B', null, false),

       -- Create the data for a HDC licence with a CAS2 address
       (2,
        '{
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
                  "bassReferral": {
                    "bassOffer": {
                     "bassArea": "Test City",
                     "postCode": "TS12 TST",
                     "telephone": "55000000000",
                     "addressTown": "Test City",
                     "addressLine1": "100 CAS2 Street",
                     "addressLine2": "The Avenue",
                     "bassAccepted": "Yes",
                     "bassOfferDetails": ""
                   },
                   "bassRequest": {
                    "specificArea": "No",
                    "bassRequested": "Yes",
                    "additionalInformation": ""
                   }
                  },
                  "proposedAddress": {
                    "optOut": {
                      "decision": "No"
                    },
                    "addressProposed": {
                      "decision": "No"
                    }
                  },
                  "curfew": {
                    "firstNight":{
                      "firstNightFrom": "15:00",
                      "firstNightUntil": "07:00"
                    },
                    "curfewHours": {
                      "fridayFrom": "19:00",
                      "mondayFrom": "19:00",
                      "sundayFrom": "19:00",
                      "fridayUntil": "07:00",
                      "mondayUntil": "07:00",
                      "sundayUntil": "07:00",
                      "tuesdayFrom": "19:00",
                      "saturdayFrom": "19:00",
                      "thursdayFrom": "19:00",
                      "tuesdayUntil": "07:00",
                      "saturdayUntil": "07:00",
                      "thursdayUntil": "07:00",
                      "wednesdayFrom": "19:00",
                      "wednesdayUntil": "07:00"
                    }
                  }
                }'::jsonb,
        98765, 'PROCESSING_RO', 1, '2021-08-06 15:06:37.188', 0, '1', '2', 'T12345D', null, false),

       -- Create the data for an approved preferred premises HDC licence
       (3,
        '{
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
                  "bassReferral": {
                   "bassRequest": {
                    "specificArea": "No",
                    "bassRequested": "No",
                    "additionalInformation": ""
                   },
                  "bassAreaCheck": {
                    "approvedPremisesRequiredYesNo": "No"
                   }
                  },
                  "proposedAddress": {
                    "optOut": {
                      "decision": "No"
                    },
                    "addressProposed": {
                      "decision": "No"
                    }
                  },
                  "curfew": {
                    "firstNight":{
                      "firstNightFrom": "15:00",
                      "firstNightUntil": "07:00"
                    },
                    "curfewHours": {
                      "fridayFrom": "19:00",
                      "mondayFrom": "19:00",
                      "sundayFrom": "19:00",
                      "fridayUntil": "07:00",
                      "mondayUntil": "07:00",
                      "sundayUntil": "07:00",
                      "tuesdayFrom": "19:00",
                      "saturdayFrom": "19:00",
                      "thursdayFrom": "19:00",
                      "tuesdayUntil": "07:00",
                      "saturdayUntil": "07:00",
                      "thursdayUntil": "07:00",
                      "wednesdayFrom": "19:00",
                      "wednesdayUntil": "07:00"
                    },
                    "approvedPremisesAddress": {
                      "addressLine1": "1 Test Street",
                      "addressLine2": "Test Area",
                      "addressTown": "Test Town",
                      "postCode": "T33 3ST"
                    },
                    "approvedPremises": {
                      "required": "Yes"
                    }
                  }
                }'::jsonb,
        12345, 'PROCESSING_RO', 1, '2021-08-06 15:06:37.188', 0, '1', '2', 'E01234F', null, false),

       -- Create the data for a CAS2 approved premises HDC licence
       (4,
        '{
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
                  "bassReferral": {
                    "approvedPremisesAddress": {
                     "addressLine1": "2 Test Road",
                     "addressTown": "Another Town",
                     "postCode": "TS7 7TS"
                    },
                   "bassRequest": {
                    "specificArea": "No",
                    "bassRequested": "Yes",
                    "additionalInformation": ""
                   },
                   "bassAreaCheck": {
                    "approvedPremisesRequiredYesNo": "Yes"
                   }
                  },
                  "proposedAddress": {
                    "optOut": {
                      "decision": "No"
                    },
                    "addressProposed": {
                      "decision": "No"
                    }
                  },
                  "curfew": {
                    "firstNight":{
                      "firstNightFrom": "15:00",
                      "firstNightUntil": "07:00"
                    },
                    "curfewHours": {
                      "fridayFrom": "19:00",
                      "mondayFrom": "19:00",
                      "sundayFrom": "19:00",
                      "fridayUntil": "07:00",
                      "mondayUntil": "07:00",
                      "sundayUntil": "07:00",
                      "tuesdayFrom": "19:00",
                      "saturdayFrom": "19:00",
                      "thursdayFrom": "19:00",
                      "tuesdayUntil": "07:00",
                      "saturdayUntil": "07:00",
                      "thursdayUntil": "07:00",
                      "wednesdayFrom": "19:00",
                      "wednesdayUntil": "07:00"
                    }
                  }
                }'::jsonb,
        43210, 'PROCESSING_RO', 1, '2021-08-06 15:06:37.188', 0, '1', '2', 'G67890H', null, false),

        -- Create a licence with no licence data
       (5,
        null,
        22222, 'PROCESSING_RO', 1, '2021-08-06 15:06:37.188', 0, '1', '2', 'I12345J', null, false);

-- audit data

INSERT INTO audit (timestamp, "user", action, details)
VALUES
    (
        '2021-08-06 15:06:37.188',
        'aCaseManager',
        'SEND',
        '{
          "bookingId": "54222",
          "transitionType": "caToRo"
        }'::jsonb
    ),
    (
        '2021-08-06 15:20:37.188',
        'creator',
        'UPDATE_SECTION',
        '{
          "bookingId": "54222",
          "transitionType": "caToRo"
        }'::jsonb
    ),
    (
        '2021-08-08 15:06:37.188',
        'submitter',
        'SEND',
        '{
          "bookingId": "54222",
          "transitionType": "roToCa"
        }'::jsonb
    ),
    (
        '2021-08-09 15:06:37.188',
        'updater',
        'UPDATE_SECTION',
        '{
          "bookingId": "54222",
          "transitionType": "caToRo"
        }'::jsonb
    ),
    (
        '2021-08-10 15:06:37.188',
        'approver',
        'SEND',
        '{
          "bookingId": "54222",
          "transitionType": "dmToCa"
        }'::jsonb
    );