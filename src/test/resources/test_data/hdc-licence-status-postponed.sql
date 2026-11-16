UPDATE licences
SET licence = jsonb_set(licence, '{finalChecks}', '{"postpone":{"decision":"Yes"}}')
WHERE booking_id = 43210;
