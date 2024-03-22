CREATE TABLE job_position_interview_type
(
    interview_type_id BIGINT NOT NULL,
    job_position_id   BIGINT NOT NULL
);

ALTER TABLE job_position_interview_type
    ADD CONSTRAINT fk_jobposinttyp_on_interview_type FOREIGN KEY (interview_type_id) REFERENCES interview_type (id);

ALTER TABLE job_position_interview_type
    ADD CONSTRAINT fk_jobposinttyp_on_job_position FOREIGN KEY (job_position_id) REFERENCES job_position (id);
