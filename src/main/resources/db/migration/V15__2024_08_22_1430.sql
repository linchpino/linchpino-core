CREATE TABLE PAYMENT_METHOD
(
    account_id  BIGINT   NOT NULL PRIMARY KEY,
    type        SMALLINT NOT NULL,
    min_payment NUMERIC(10, 2),
    max_payment NUMERIC(10, 2),
    fix_rate    NUMERIC(10, 2),
    CONSTRAINT FK_PAYMENT_METHOD_ON_ACCOUNT FOREIGN KEY (account_id) REFERENCES ACCOUNT (id)
);

ALTER TABLE PAYMENT_METHOD
    ADD CONSTRAINT check_min_payment_non_negative CHECK (min_payment >= 0);

ALTER TABLE PAYMENT_METHOD
    ADD CONSTRAINT check_max_payment_limit CHECK (max_payment <= 1000.0);

ALTER TABLE PAYMENT_METHOD
    ADD CONSTRAINT check_pay_as_you_go_limits CHECK (
        (type = 1 AND min_payment IS NOT NULL AND max_payment IS NOT NULL)
            OR (type != 1 AND min_payment IS NULL AND max_payment IS NULL)
        );

ALTER TABLE PAYMENT_METHOD
    ADD CONSTRAINT check_fix_rate_for_fixed_price CHECK (
        (type = 2 AND fix_rate IS NOT NULL)
            OR (type != 2 AND fix_rate IS NULL)
        );

ALTER TABLE PAYMENT_METHOD
    ADD CONSTRAINT check_free_no_payments CHECK (
        (type = 3 AND min_payment IS NULL AND max_payment IS NULL AND fix_rate IS NULL)
        );
