ALTER TABLE mentor_time_slot ADD COLUMN status VARCHAR(50) DEFAULT 'DRAFT' NOT NULL;

CREATE TABLE account_interview_type
(
    account_id        BIGINT NOT NULL,
    interview_type_id BIGINT NOT NULL,
    CONSTRAINT pk_account_interview_type PRIMARY KEY (account_id, interview_type_id)
);

ALTER TABLE account_interview_type
    ADD CONSTRAINT fk_accinttyp_on_account FOREIGN KEY (account_id) REFERENCES account (id);

ALTER TABLE account_interview_type
    ADD CONSTRAINT fk_accinttyp_on_interview_type FOREIGN KEY (interview_type_id) REFERENCES interview_type (id);
