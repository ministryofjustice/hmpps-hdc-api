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

-- Row 1: Approved required curfew.approvedPremisesAddress
(
    1,
    '{
        "curfew": {
            "approvedPremises": { "required": "Yes" },
            "approvedPremisesAddress": {
                "addressLine1": "TEST_APPROVED_PRIMARY_1",
                "addressLine2": "TEST_APPROVED_PRIMARY_2",
                "addressTown": "TEST_TOWN",
                "postCode": "ZZ1 1ZZ",
                "telephone": "07000000001"
            }
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

-- Row 2: Approved not required use bassReferral.approvedPremisesAddress if present
(
    2,
    '{
        "curfew": {
            "approvedPremises": { "required": "No" }
        },
        "bassReferral": {
            "approvedPremisesAddress": {
                "addressLine1": "TEST_APPROVED_PRIMARY_1",
                "addressLine2": "TEST_APPROVED_PRIMARY_2",
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

-- Row 3: No address anywhere expect NULL address
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

-- Row 4: CAS2 offer present use bassOffer
(
    4,
    '{
        "curfew": {
            "approvedPremises": { "required": "No" }
        },
        "bassReferral": {
            "bassOffer": {
                "addressLine1": "TEST_CAS2_PRIMARY_1",
                "addressLine2": "TEST_CAS2_PRIMARY_2",
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

-- Row 5: Approved not required + no CAS2 use proposedAddress.curfewAddress
(
    5,
    '{
        "proposedAddress": {
            "curfewAddress": {
                "addressLine1": "TEST_PROPOSED_PRIMARY_1",
                "addressLine2": "TEST_PROPOSED_PRIMARY_2",
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

INSERT INTO audit (timestamp, "user", action, details)
VALUES
    (
        '2021-04-05 15:06:37.188',
        'creator',
        'LICENCE_RECORD_STARTED',
        '{
          "bookingId": "10001"
        }'::jsonb
    ),
    (
        '2021-04-05 15:06:37.188',
        'creator',
        'LICENCE_RECORD_STARTED',
        '{
          "bookingId": "10002"
        }'::jsonb
    ),
    (
        '2021-04-05 15:06:37.188',
        'creator',
        'LICENCE_RECORD_STARTED',
        '{
          "bookingId": "10003"
        }'::jsonb
    ),
    (
        '2021-04-05 15:06:37.188',
        'creator',
        'LICENCE_RECORD_STARTED',
        '{
          "bookingId": "10004"
        }'::jsonb
    ),
    (
        '2021-04-05 15:06:37.188',
        'creator',
        'LICENCE_RECORD_STARTED',
        '{
          "bookingId": "10005"
        }'::jsonb
    );


