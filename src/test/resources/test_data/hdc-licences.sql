INSERT INTO licences
(id, licence, booking_id, stage, "version", transition_date, vary_version, additional_conditions_version,
 standard_conditions_version, prison_number, deleted_at, licence_in_cvl)

-- Create the data for a HDC licence with a preferred address
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
                      "postCode": "TST1 1TS",
                      "residents": [],
                      "telephone": "44000000000",
                      "addressTown": "Test City",
                      "addressLine1": "123 Approved Premises Street 2",
                      "addressLine2": "Off Test Place",
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
        54321, 'PROCESSING_RO', 1, '2021-08-06 15:06:37.188', 0, '1', '2', 'A12345B', null, false),

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

