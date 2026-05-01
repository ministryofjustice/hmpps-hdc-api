INSERT INTO licences
(id, licence, booking_id, stage, "version", transition_date, vary_version, additional_conditions_version,
 standard_conditions_version, prison_number, deleted_at, licence_in_cvl)

-- Create the data for a HDC licence with a preferred address
VALUES (1,
        '{
                  "document": {
                    "template": {
                      "decision": "hdc_ap",
                      "offenceCommittedBeforeFeb2015": "No"
                    }
                  },
                  "curfew": {
                    "firstNight":{
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
            },
            "additional": {
                "NO_CAMERA": {},
                "NO_RESIDE": {
                    "notResideWithAge": "20",
                    "notResideWithGender": "any male"
                },
                "REPORT_TO": {
                    "reportingTime": "12:30",
                    "reportingAddress": "Sheffield Police Station",
                    "reportingFrequency": "Weekly"
                },
                "ATTEND_ALL": {
                    "appointmentProfessions": "psychiatrist"
                },
                "HOME_VISITS": {
                    "mentalHealthName": "Jim Smith"
                },
                "DRUG_TESTING": {
                    "drug_testing_name": "Bobby",
                    "drug_testing_address": "Smith Street Centre"
                },
                "EXCLUSION_AREA": {
                    "exclusionArea": "Rugby"
                },
                "REMAIN_ADDRESS": {
                    "curfewTo": "16:30",
                    "curfewFrom": "12:30",
                    "curfewAddress": "21 Smith Street\\r\\nSmith Town",
                    "curfewTagRequired": "yes"
                },
                "CONFINE_ADDRESS": {
                    "confinedTo": "12:20",
                    "confinedFrom": "16:30",
                    "confinedReviewFrequency": "Other"
                },
                "VEHICLE_DETAILS": {
                    "vehicleDetails": "Red Estate Car"
                },
                "NO_CONTACT_NAMED": {
                    "noContactOffenders": "James, Johnny"
                },
                "NO_WORK_WITH_AGE": {
                    "noWorkWithAge": "20"
                },
                "ATTEND_DEPENDENCY": {
                    "appointmentDate": "12/03/2026",
                    "appointmentTime": "12:21",
                    "appointmentAddress": "Smith Street\\r\\nS1 1AA"
                },
                "EXCLUSION_ADDRESS": {
                    "noEnterPlace": "The Crown on Smith Street, LS1 3GH"
                },
                "NO_CHILDRENS_AREA": {
                    "notInSightOf": "Swimming pool"
                },
                "ALCOHOL_MONITORING": {
                    "endDate": "12/11/2026",
                    "timeframe": "4 weeks"
                },
                "COMPLY_REQUIREMENTS": {
                    "courseOrCentre": "Stop drinking course",
                    "abuseAndBehaviours": [
                        "alcohol",
                        "sexual",
                        "gambling",
                        "debt"
                    ]
                },
                "NO_CONTACT_ASSOCIATE": {
                    "groupsOrOrganisation": "The Tables"
                },
                "INTIMATE_RELATIONSHIP": {
                    "intimateGender": "Women or men"
                },
                "NO_COMMUNICATE_VICTIM": {
                    "socialServicesDept": "Harrow Street",
                    "victimFamilyMembers": "John and James"
                },
                "NO_UNSUPERVISED_CONTACT": {
                    "unsupervisedContactAge": "20",
                    "unsupervisedContactGender": "any male",
                    "unsupervisedContactSocial": "Harrow Street"
                },
                "RESIDE_AT_SPECIFIC_PLACE": {
                    "region": "wales"
                },
                "CURFEW_UNTIL_INSTALLATION": {
                    "approvedAddress": "21 Smith Street,\\r\\nSmith Town,\\r\\nS12 1AA"
                },
                "ELECTRONIC_MONITORING_TRAIL": {
                    "trailEndDate": "12/12/2026"
                },
                "ATTEND_DEPENDENCY_IN_DRUGS_SECTION": {
                    "appointmentDateInDrugsSection": "12/12/2026",
                    "appointmentTimeInDrugsSection": "12:32",
                    "appointmentAddressInDrugsSection": "Some Street\\r\\nLS1 1AB"
                },
                "ELECTRONIC_MONITORING_INSTALLATION": {
                    "conditionTypes": "alcohol, smoking"
                }
            },
            "conditionsSummary": {
                "additionalConditionsJustification": "adsas"
            }
        }
                }'::jsonb,
        10, 'DECIDED', 1, '2021-08-06 15:06:37.188', 0, '2', '2', 'A1234EE', null, false),

       -- Create the data for a HDC licence with a CAS2 address
       (2,
        '{
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
                   "wednesdayUntil": "07:00",
                   "daySpecificInputs": "Yes"
                 }
               }
             }'::jsonb,
        20, 'DECIDED', 1, '2021-08-06 15:06:37.188', 0, '1', '2', 'B1234EE', null, false),

       -- Create the data for an approved preferred premises HDC licence
       (3,
        '{
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
                      "wednesdayUntil": "07:00",
                      "daySpecificInputs": "Yes"
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
        30, 'DECIDED', 1, '2021-08-06 15:06:37.188', 0, '1', '2', 'C1234EE', null, false);


-- audit data

INSERT INTO audit (timestamp, "user", action, details)
VALUES
    (
        '2021-04-05 15:06:37.188',
        'creator',
        'LICENCE_RECORD_STARTED',
        '{
          "bookingId": "10"
        }'::jsonb
    ),
    (
        '2021-04-05 15:07:37.188',
        'aCaseManager',
        'SEND',
        '{
          "bookingId": "10",
          "transitionType": "caToRo"
        }'::jsonb
    ),
    (
        '2021-08-06 15:04:37.188',
        'creator',
        'RESET',
        '{
          "bookingId": "10"
        }'::jsonb
    ),
    (
        '2021-08-06 15:05:37.188',
        'creator',
        'LICENCE_RECORD_STARTED',
        '{
          "bookingId": "10"
        }'::jsonb
    ),
    (
        '2021-08-06 15:06:37.188',
        'aCaseManager',
        'SEND',
        '{
          "bookingId": "10",
          "transitionType": "caToRo"
        }'::jsonb
    ),
    (
        '2021-08-06 15:20:37.188',
        'creator',
        'UPDATE_SECTION',
        '{
          "bookingId": "10",
          "transitionType": "caToRo"
        }'::jsonb
    ),
    (
        '2021-08-08 15:06:37.188',
        'submitter',
        'SEND',
        '{
          "bookingId": "10",
          "transitionType": "roToCa"
        }'::jsonb
    ),
    (
        '2021-08-09 15:06:37.188',
        'updater',
        'UPDATE_SECTION',
        '{
          "bookingId": "10",
          "transitionType": "caToRo"
        }'::jsonb
    ),
    (
        '2021-08-10 15:06:37.188',
        'approver',
        'SEND',
        '{
          "bookingId": "10",
          "transitionType": "dmToCa"
        }'::jsonb
    ),
    (
        '2021-04-05 15:06:37.188',
        'creator',
        'LICENCE_RECORD_STARTED',
        '{
          "bookingId": "20"
        }'::jsonb
    ),
    (
        '2021-04-05 15:06:37.188',
        'creator',
        'LICENCE_RECORD_STARTED',
        '{
          "bookingId": "30"
        }'::jsonb
    );
