CREATE TABLE payment
(
    interview_id BIGINT                      NOT NULL,
    status       VARCHAR(255),
    ref_number   VARCHAR(255)                NOT NULL,
    amount       DECIMAL(19, 2)              NOT NULL DEFAULT 0,
    created_by   BIGINT,
    created_on   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    modified_by  BIGINT,
    modified_on  TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_payment PRIMARY KEY (interview_id)
);

ALTER TABLE payment
    ADD CONSTRAINT FK_PAYMENT_ON_INTERVIEW FOREIGN KEY (interview_id) REFERENCES interview (id);
