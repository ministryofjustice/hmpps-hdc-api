INSERT INTO licences (
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

-- Row 1: AP required → curfew.approvedPremisesAddress
(
    1,
    '{
        "curfew": {
            "approvedPremises": { "required": "Yes" },
            "approvedPremisesAddress": {
                "addressLine1": "TEST_AP_PRIMARY_1",
                "addressLine2": "TEST_AP_PRIMARY_2",
                "addressTown": "TEST_TOWN",
                "postCode": "ZZ1 1ZZ",
                "telephone": "07000000001"
            }
        },
        "bassReferral": {
            "bassOffer": {
                "addressLine1": "TEST_BASS_SHOULD_NOT_BE_USED",
                "addressLine2": "TEST_BASS_SHOULD_NOT_BE_USED",
                "addressTown": "TEST_TOWN",
                "postCode": "ZZ1 1ZZ",
                "telephone": "07000000002",
                "bassArea": "TEST_AREA",
                "bassAccepted": "Yes"
            },
            "bassRequest": { "bassRequested": "Yes" },
            "bassAreaCheck": { "approvedPremisesRequiredYesNo": "No" }
        },
        "approval": {
            "release": {
                "decision": "Yes",
                "decisionMaker": "TEST_USER",
                "reasonForDecision": "TEST_REASON"
            },
            "consideration": { "decision": "Yes" }
        },
        "document": {
            "template": {
                "decision": "hdc_ap",
                "offenceCommittedBeforeFeb2015": "No"
            }
        }
    }'::jsonb,
    10001,
    'ELIGIBILITY',
    1,
    '2021-01-01 00:00:00',
    0,
    1,
    1,
    'TST001',
    NULL,
    FALSE
),

-- Row 2: AP not required → use bassReferral.approvedPremisesAddress if present
(
    2,
    '{
        "curfew": {
            "approvedPremises": { "required": "No" }
        },
        "bassReferral": {
            "approvedPremisesAddress": {
                "addressLine1": "TEST_CAS2_AP_PRIMARY_1",
                "addressLine2": "TEST_CAS2_AP_PRIMARY_2",
                "addressTown": "TEST_TOWN",
                "postCode": "ZZ1 1ZZ",
                "telephone": "07000000003"
            },
            "bassRequest": { "bassRequested": "Yes" },
            "bassAreaCheck": { "approvedPremisesRequiredYesNo": "Yes" }
        },
        "approval": {
            "release": {
                "decision": "Yes",
                "decisionMaker": "TEST_USER",
                "reasonForDecision": "TEST_REASON"
            },
            "consideration": { "decision": "Yes" }
        },
        "document": {
            "template": {
                "decision": "hdc_ap",
                "offenceCommittedBeforeFeb2015": "No"
            }
        }
    }'::jsonb,
    10002,
    'ELIGIBILITY',
    1,
    '2021-01-01 00:00:00',
    0,
    1,
    1,
    'TST002',
    NULL,
    FALSE
),

-- Row 3: No address anywhere → expect NULL fallback
(
    3,
    '{
        "document": {
            "template": {
                "decision": "hdc_ap",
                "offenceCommittedBeforeFeb2015": "No"
            }
        }
    }'::jsonb,
    10003,
    'ELIGIBILITY',
    1,
    '2021-01-01 00:00:00',
    0,
    1,
    1,
    'TST003',
    NULL,
    FALSE
),

-- Row 4: CAS2 offer present → use bassOffer
(
    4,
    '{
        "curfew": {
            "approvedPremises": { "required": "No" }
        },
        "bassReferral": {
            "bassOffer": {
                "addressLine1": "TEST_BASS_PRIMARY_1",
                "addressLine2": "TEST_BASS_PRIMARY_2",
                "addressTown": "TEST_TOWN",
                "postCode": "ZZ1 1ZZ",
                "telephone": "07000000004",
                "bassArea": "TEST_AREA",
                "bassAccepted": "Yes"
            },
            "bassRequest": { "bassRequested": "Yes" },
            "bassAreaCheck": { "approvedPremisesRequiredYesNo": "No" }
        },
        "approval": {
            "release": {
                "decision": "Yes",
                "decisionMaker": "TEST_USER",
                "reasonForDecision": "TEST_REASON"
            },
            "consideration": { "decision": "Yes" }
        },
        "document": {
            "template": {
                "decision": "hdc_ap",
                "offenceCommittedBeforeFeb2015": "No"
            }
        }
    }'::jsonb,
    10004,
    'ELIGIBILITY',
    1,
    '2021-01-01 00:00:00',
    0,
    1,
    1,
    'TST004',
    NULL,
    FALSE
),

-- Row 5: No AP + no CAS2 → fallback to proposedAddress.curfewAddress
(
    5,
    '{
        "proposedAddress": {
            "curfewAddress": {
                "addressLine1": "TEST_FALLBACK_PRIMARY_1",
                "addressLine2": "TEST_FALLBACK_PRIMARY_2",
                "addressTown": "TEST_CITY",
                "postCode": "ZZ1 1ZZ",
                "telephone": "07000000005"
            }
        },
        "curfew": {
            "approvedPremises": { "required": "No" }
        },
        "bassReferral": {
            "bassRequest": { "bassRequested": "Yes" },
            "bassAreaCheck": { "approvedPremisesRequiredYesNo": "No" }
        },
        "approval": {
            "release": {
                "decision": "Yes",
                "decisionMaker": "TEST_USER",
                "reasonForDecision": "TEST_REASON"
            },
            "consideration": { "decision": "Yes" }
        },
        "document": {
            "template": {
                "decision": "hdc_ap",
                "offenceCommittedBeforeFeb2015": "No"
            }
        }
    }'::jsonb,
    10005,
    'ELIGIBILITY',
    1,
    '2021-01-01 00:00:00',
    0,
    1,
    1,
    'TST005',
    NULL,
    FALSE
);
