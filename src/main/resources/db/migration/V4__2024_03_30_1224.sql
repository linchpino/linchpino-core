ALTER TABLE mentor_time_slot
    RENAME COLUMN TO_TOME TO TO_TIME;

ALTER TABLE mentor_time_slot
    DROP COLUMN date;

ALTER TABLE mentor_time_slot
    DROP COLUMN status;
ALTER TABLE mentor_time_slot
    ADD COLUMN status SMALLINT DEFAULT 2; -- DRAFT = 2

ALTER TABLE account
    DROP COLUMN status;
ALTER TABLE account
    DROP COLUMN type;
ALTER TABLE account
    ADD COLUMN status SMALLINT DEFAULT 2; -- DEACTIVATED = 2
ALTER TABLE account
    ADD COLUMN type SMALLINT DEFAULT 3; -- UNKNOWN = 3


