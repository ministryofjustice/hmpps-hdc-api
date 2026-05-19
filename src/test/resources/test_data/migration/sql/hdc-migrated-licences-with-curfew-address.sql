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

-- Row 1: Approved required curfew.approvedPremisesAddress
(
    1,
    'hdc_ap',
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
    1,
    0,
    'TST001',
    null,
    False,
    NOW()),

-- Row 2: Approved not required use bassReferral.approvedPremisesAddress if present
(
    2,
    'hdc_ap',
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
    1,
    0,
    'TST002',
    null,
    False,
    NOW()
),

-- Row 3: No address anywhere expect NULL address
(
    3,
    'hdc_ap',
    '{
        "document": {
            "template": {
                "decision": "hdc_ap",
                "offenceCommittedBeforeFeb2015": "No"
            }
        }
    }'::jsonb,
    10003,
    1,
    0,
    'TST003',
    null,
    False,
    NOW()
),

-- Row 4: CAS2 offer present use bassOffer
(
    4,
    'hdc_ap',
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
    1,
    0,
    'TST004',
    NULL,
    FALSE,
    NOW()),

-- Row 5: Approved not required + no CAS2 use proposedAddress.curfewAddress
(
    5,
    'hdc_ap',
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
    1,
    0,
    'TST005',
    NULL,
    FALSE,
    NOW()
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

