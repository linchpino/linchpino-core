-- rename duplicates before creating unique constraint
UPDATE interview_type
SET name = name || '_' || id
WHERE name IN (
    SELECT name
    FROM interview_type
    GROUP BY name
    HAVING COUNT(*) > 1
);

ALTER TABLE interview_type
    ADD CONSTRAINT uc_interviewtype_name UNIQUE (NAME);
