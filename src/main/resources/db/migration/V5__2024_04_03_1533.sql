DROP TABLE mentor_interview_type;

UPDATE account SET created_by = NULL;
UPDATE account SET modified_by = NULL;
ALTER TABLE account ALTER COLUMN created_by TYPE BIGINT USING created_by::bigint;
ALTER TABLE account ALTER COLUMN modified_by TYPE BIGINT USING modified_by::bigint;


UPDATE interview SET created_by = NULL;
UPDATE interview SET modified_by = NULL;
ALTER TABLE interview ALTER COLUMN created_by TYPE BIGINT USING created_by::bigint;
ALTER TABLE interview ALTER COLUMN modified_by TYPE BIGINT USING modified_by::bigint;

UPDATE interview_type SET created_by = NULL;
UPDATE interview_type SET modified_by = NULL;
ALTER TABLE interview_type ALTER COLUMN created_by TYPE BIGINT USING created_by::bigint;
ALTER TABLE interview_type ALTER COLUMN modified_by TYPE BIGINT USING modified_by::bigint;

UPDATE job_position SET created_by = NULL;
UPDATE job_position SET modified_by = NULL;
ALTER TABLE job_position ALTER COLUMN created_by TYPE BIGINT USING created_by::bigint;
ALTER TABLE job_position ALTER COLUMN modified_by TYPE BIGINT USING modified_by::bigint;

UPDATE mentor_time_slot SET created_by = NULL;
UPDATE mentor_time_slot SET modified_by = NULL;
ALTER TABLE mentor_time_slot ALTER COLUMN created_by TYPE BIGINT USING created_by::bigint;
ALTER TABLE mentor_time_slot ALTER COLUMN modified_by TYPE BIGINT USING modified_by::bigint;
