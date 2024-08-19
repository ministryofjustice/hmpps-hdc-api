INSERT INTO licences
(id, licence, booking_id, stage, "version", transition_date, vary_version, additional_conditions_version,
 standard_conditions_version, prison_number, deleted_at)
VALUES (1,
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
              "bassRequested": "No"
            }
          },
          "proposedAddress": {
            "optOut": {
              "decision": "No"
            },
            "curfewAddress": {
              "occupier": {
                "name": "Some Duty Officer",
                "isOffender": "Yes",
                "relationship": ""
              },
              "postCode": "LS1 2AA",
              "residents": [],
              "telephone": "44000000000",
              "addressTown": "Leeds",
              "addressLine1": "123 Approved Premises Street 2",
              "addressLine2": "Off St Michaels Place",
              "additionalInformation": "",
              "residentOffenceDetails": "",
              "cautionedAgainstResident": "No"
            },
            "addressProposed": {
              "decision": "Yes"
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
        54321, 'PROCESSING_RO', 1, '2021-08-06 15:06:37.188', 0, '1', '2', 'A12345B', null),
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
                     "bassArea": "Leeds",
                     "postCode": "LS3 4BB",
                     "telephone": "55000000000",
                     "addressTown": "Leeds",
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
        98765, 'PROCESSING_RO', 1, '2021-08-06 15:06:37.188', 0, '1', '2', 'C56789D', null);
