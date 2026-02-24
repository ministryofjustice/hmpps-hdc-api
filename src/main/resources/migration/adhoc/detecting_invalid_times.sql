SELECT '-- ID ' || c.id  ||' Original value (' || c.value || ')' || E'\n' || 
       'UPDATE licences l SET licence = jsonb_set( l.licence,''{' || path1 || ',' || path2 || ',' || path3 || '}'', ' ||
        CASE
            WHEN c.value = '' OR c.value = 'NONE' THEN '''null''::jsonb'
            ELSE format(
                '''%s:%s''::jsonb',
                lpad(split_part(replace(c.trimmedValue, '.', ':'), ':', 1), 2, '0'),
                lpad(split_part(replace(c.trimmedValue, '.', ':'), ':', 2), 2, '0')
            )
        END
        || ', false ) WHERE l.id = ' || c.id || ' and ' || 'jsonb_path_exists(l.licence,'' $.' || path1 || '.' || path2 ||'.' || path3 || ' ? (@ == "")'');'  AS update_sql

FROM (
         SELECT trim(v.value) as trimmedValue,
                v.value as value,
           l.id as id,
           path1, path2, path3
         FROM licences l
             CROSS JOIN LATERAL (
             VALUES
             ('curfew','firstNight','firstNightFrom',  l.licence #>> '{curfew,firstNight,firstNightFrom}'),
             ('curfew','firstNight','firstNightUntil', l.licence #>> '{curfew,firstNight,firstNightUntil}'),
             ('curfew','curfewHours','allFrom',        l.licence #>> '{curfew,curfewHours,allFrom}'),
             ('curfew','curfewHours','allUntil',       l.licence #>> '{curfew,curfewHours,allUntil}'),
             ('curfew','curfewHours','mondayFrom',     l.licence #>> '{curfew,curfewHours,mondayFrom}'),
             ('curfew','curfewHours','mondayUntil',    l.licence #>> '{curfew,curfewHours,mondayUntil}'),
             ('curfew','curfewHours','tuesdayFrom',    l.licence #>> '{curfew,curfewHours,tuesdayFrom}'),
             ('curfew','curfewHours','tuesdayUntil',   l.licence #>> '{curfew,curfewHours,tuesdayUntil}'),
             ('curfew','curfewHours','wednesdayFrom',  l.licence #>> '{curfew,curfewHours,wednesdayFrom}'),
             ('curfew','curfewHours','wednesdayUntil', l.licence #>> '{curfew,curfewHours,wednesdayUntil}'),
             ('curfew','curfewHours','thursdayFrom',   l.licence #>> '{curfew,curfewHours,thursdayFrom}'),
             ('curfew','curfewHours','thursdayUntil',  l.licence #>> '{curfew,curfewHours,thursdayUntil}'),
             ('curfew','curfewHours','fridayFrom',     l.licence #>> '{curfew,curfewHours,fridayFrom}'),
             ('curfew','curfewHours','fridayUntil',    l.licence #>> '{curfew,curfewHours,fridayUntil}'),
             ('curfew','curfewHours','saturdayFrom',   l.licence #>> '{curfew,curfewHours,saturdayFrom}'),
             ('curfew','curfewHours','saturdayUntil',  l.licence #>> '{curfew,curfewHours,saturdayUntil}'),
             ('curfew','curfewHours','sundayFrom',     l.licence #>> '{curfew,curfewHours,sundayFrom}'),
             ('curfew','curfewHours','sundayUntil',    l.licence #>> '{curfew,curfewHours,sundayUntil}')
             ) v(path1, path2, path3, value)
         WHERE v.value IS NOT NULL
           AND (trim(v.value) = '' OR v.value !~ '^\d{2}:\d{2}$')
     ) c
WHERE trimmedValue = '';

-- to detect empty values in curfew json
SELECT COUNT(*) FROM licences l WHERE (
              jsonb_path_exists(l.licence, '$.curfew.firstNight.firstNightFrom ? (@ == "")')
                  OR jsonb_path_exists(l.licence, '$.curfew.firstNight.firstNightUntil ? (@ == "")')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.allFrom ? (@ == "")')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.allUntil ? (@ == "")')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.mondayFrom ? (@ == "")')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.mondayUntil ? (@ == "")')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.tuesdayFrom ? (@ == "")')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.tuesdayUntil ? (@ == "")')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.wednesdayFrom ? (@ == "")')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.wednesdayUntil ? (@ == "")')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.thursdayFrom ? (@ == "")')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.thursdayUntil ? (@ == "")')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.fridayFrom ? (@ == "")')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.fridayUntil ? (@ == "")')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.saturdayFrom ? (@ == "")')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.saturdayUntil ? (@ == "")')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.sundayFrom ? (@ == "")')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.sundayUntil ? (@ == "")')
              );

-- to detect null values in curfew json
SELECT COUNT(*) FROM licences l
    WHERE (
              jsonb_path_exists(l.licence, '$.curfew.firstNight.firstNightFrom ? (@ == null)')
                  OR jsonb_path_exists(l.licence, '$.curfew.firstNight.firstNightUntil ? (@ == null)')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.allFrom ? (@ == null)')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.allUntil ? (@ == null)')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.mondayFrom ? (@ == null)')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.mondayUntil ? (@ == null)')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.tuesdayFrom ? (@ == null)')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.tuesdayUntil ? (@ == null)')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.wednesdayFrom ? (@ == null)')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.wednesdayUntil ? (@ == null)')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.thursdayFrom ? (@ == null)')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.thursdayUntil ? (@ == null)')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.fridayFrom ? (@ == null)')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.fridayUntil ? (@ == null)')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.saturdayFrom ? (@ == null)')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.saturdayUntil ? (@ == null)')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.sundayFrom ? (@ == null)')
                  OR jsonb_path_exists(l.licence, '$.curfew.curfewHours.sundayUntil ? (@ == null)')
              );