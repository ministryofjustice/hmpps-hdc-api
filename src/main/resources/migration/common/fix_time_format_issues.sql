BEGIN;

	-- Create temp table to hold time data to be updated

    DROP TABLE IF EXISTS licence_time_fix;

    CREATE TEMP TABLE licence_time_fix (
            id BIGINT,
            json_path TEXT[],
            new_value TEXT,
            old_value TEXT
        );

    -- Populate temp data with columns that need to be change inc Empty,NONE, 0 (these will be set to null)

    INSERT INTO licence_time_fix (id, new_value, old_value, json_path)
    SELECT *
    FROM (
             SELECT
                 l.id,
                 CASE
                     WHEN v.value ~ '^\s*(\d{1,2})[:\.](\d{2})(:\d{2})?\s*$' THEN
                         lpad(regexp_replace(v.value, '^\s*(\d{1,2}).*', '\1'), 2, '0')
                             || ':' ||
                         lpad(regexp_replace(v.value, '.*[:\.](\d{2}).*', '\1'), 2, '0')
                     END AS new_value,
                 v.value AS old_value,
                 CASE v.key
                     WHEN 'firstNightFrom' THEN ARRAY['curfew','firstNight','firstNightFrom']
                     WHEN 'firstNightUntil' THEN ARRAY['curfew','firstNight','firstNightUntil']
                     WHEN 'allFrom' THEN ARRAY['curfew','curfewHours','allFrom']
                     WHEN 'allUntil' THEN ARRAY['curfew','curfewHours','allUntil']
                     WHEN 'mondayFrom' THEN ARRAY['curfew','curfewHours','mondayFrom']
                     WHEN 'mondayUntil' THEN ARRAY['curfew','curfewHours','mondayUntil']
                     WHEN 'tuesdayFrom' THEN ARRAY['curfew','curfewHours','tuesdayFrom']
                     WHEN 'tuesdayUntil' THEN ARRAY['curfew','curfewHours','tuesdayUntil']
                     WHEN 'wednesdayFrom' THEN ARRAY['curfew','curfewHours','wednesdayFrom']
                     WHEN 'wednesdayUntil' THEN ARRAY['curfew','curfewHours','wednesdayUntil']
                     WHEN 'thursdayFrom' THEN ARRAY['curfew','curfewHours','thursdayFrom']
                     WHEN 'thursdayUntil' THEN ARRAY['curfew','curfewHours','thursdayUntil']
                     WHEN 'fridayFrom' THEN ARRAY['curfew','curfewHours','fridayFrom']
                     WHEN 'fridayUntil' THEN ARRAY['curfew','curfewHours','fridayUntil']
                     WHEN 'saturdayFrom' THEN ARRAY['curfew','curfewHours','saturdayFrom']
                     WHEN 'saturdayUntil' THEN ARRAY['curfew','curfewHours','saturdayUntil']
                     WHEN 'sundayFrom' THEN ARRAY['curfew','curfewHours','sundayFrom']
                     WHEN 'sundayUntil' THEN ARRAY['curfew','curfewHours','sundayUntil']
                     END AS json_path
             FROM licences l
                      CROSS JOIN LATERAL (
                 VALUES
                     ('firstNightFrom', l.licence #>> '{curfew,firstNight,firstNightFrom}'),
                     ('firstNightUntil', l.licence #>> '{curfew,firstNight,firstNightUntil}'),
                     ('allFrom', l.licence #>> '{curfew,curfewHours,allFrom}'),
                     ('allUntil', l.licence #>> '{curfew,curfewHours,allUntil}'),
                     ('mondayFrom', l.licence #>> '{curfew,curfewHours,mondayFrom}'),
                     ('mondayUntil', l.licence #>> '{curfew,curfewHours,mondayUntil}'),
                     ('tuesdayFrom', l.licence #>> '{curfew,curfewHours,tuesdayFrom}'),
                     ('tuesdayUntil', l.licence #>> '{curfew,curfewHours,tuesdayUntil}'),
                     ('wednesdayFrom', l.licence #>> '{curfew,curfewHours,wednesdayFrom}'),
                     ('wednesdayUntil', l.licence #>> '{curfew,curfewHours,wednesdayUntil}'),
                     ('thursdayFrom', l.licence #>> '{curfew,curfewHours,thursdayFrom}'),
                     ('thursdayUntil', l.licence #>> '{curfew,curfewHours,thursdayUntil}'),
                     ('fridayFrom', l.licence #>> '{curfew,curfewHours,fridayFrom}'),
                     ('fridayUntil', l.licence #>> '{curfew,curfewHours,fridayUntil}'),
                     ('saturdayFrom', l.licence #>> '{curfew,curfewHours,saturdayFrom}'),
                     ('saturdayUntil', l.licence #>> '{curfew,curfewHours,saturdayUntil}'),
                     ('sundayFrom', l.licence #>> '{curfew,curfewHours,sundayFrom}'),
                     ('sundayUntil', l.licence #>> '{curfew,curfewHours,sundayUntil}')
                     ) v(key, value)

             WHERE l.id IN (
                            526,3801,4964,5368,5618,5818,8729,9733,10396,11439,
                            11773,12731,13315,13960,17246,19512,19881,19885,20412,
                            20731,20816,21944,23344,23763,25280,26875,29869,30623,
                            31041,35115,36963,38097,45353,46234,48111,49252,53806,
                            57161,59268,62064,67442,68937,72399,73281,75585,82078,
                            88110,88371,89802,90303,105176,106439,107618,114870,
                            123098,136792,138183,138797,139296,146071,147707,152837,
                            158245,162112,162358,164098,177203
                 )
         ) tmp WHERE tmp.old_value IS DISTINCT FROM tmp.new_value AND tmp.json_path IS NOT null
    ORDER BY tmp.id;

    SELECT id,new_value,old_value,array_to_string(json_path, '->') AS json_path_string FROM licence_time_fix;

    -- Apply update -- run this after checking data using the above select statement
    /*

    UPDATE licences l
        SET licence = jsonb_set(
                l.licence,
                t.json_path,
                to_jsonb(t.new_value),
                true
            )
        FROM licence_time_fix t
            WHERE l.id = t.id AND t.new_value IS NOT NULL;
    */

-- Commit time change
COMMIT;
