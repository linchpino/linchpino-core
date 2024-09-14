ALTER TABLE job_position
    ADD CONSTRAINT uc_jobposition_title UNIQUE (TITLE);
