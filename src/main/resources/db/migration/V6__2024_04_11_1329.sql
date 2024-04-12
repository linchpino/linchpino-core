CREATE TABLE role (
    id BIGINT NOT NULL,
    roleName SMALLINT DEFAULT 1,; -- GUEST = 1
    accounts list
);

CREATE TABLE account_role (
    account_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    FOREIGN KEY (account_id) REFERENCES account(id),
    FOREIGN KEY (role_id) REFERENCES role(id),
    PRIMARY KEY (account_id, role_id)
);
