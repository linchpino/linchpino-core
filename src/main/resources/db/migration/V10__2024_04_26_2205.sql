ALTER TABLE account
    ALTER COLUMN email SET NOT NULL,
    ADD CONSTRAINT uk_account_email UNIQUE (email);
