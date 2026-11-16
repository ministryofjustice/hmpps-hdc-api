UPDATE licences
SET licence = jsonb_set(licence, '{proposedAddress,optOut,decision}', '"Yes"')
WHERE booking_id = 43210;
