CREATE DATABASE IF NOT EXISTS banksphere;
USE banksphere;

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(40) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_roles_name (name)
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    email VARCHAR(160) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(40),
    address VARCHAR(260),
    enabled BIT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE IF NOT EXISTS bank_accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_number VARCHAR(20) NOT NULL,
    account_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    balance DECIMAL(19, 2) NOT NULL,
    version BIGINT,
    user_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_bank_accounts_number (account_number),
    KEY idx_bank_account_user (user_id),
    CONSTRAINT fk_bank_accounts_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS transfers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    transfer_reference VARCHAR(80) NOT NULL,
    from_account_id BIGINT NOT NULL,
    to_account_id BIGINT NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    description VARCHAR(260),
    status VARCHAR(30) NOT NULL,
    idempotency_key VARCHAR(120) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_transfers_reference (transfer_reference),
    UNIQUE KEY uk_transfers_idempotency (idempotency_key),
    KEY idx_transfer_from_account (from_account_id),
    KEY idx_transfer_to_account (to_account_id),
    CONSTRAINT fk_transfers_from_account FOREIGN KEY (from_account_id) REFERENCES bank_accounts (id),
    CONSTRAINT fk_transfers_to_account FOREIGN KEY (to_account_id) REFERENCES bank_accounts (id)
);

CREATE TABLE IF NOT EXISTS bank_transactions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    transaction_reference VARCHAR(80) NOT NULL,
    account_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    balance_after DECIMAL(19, 2) NOT NULL,
    description VARCHAR(260),
    transfer_id BIGINT,
    occurred_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_bank_transactions_reference (transaction_reference),
    KEY idx_transaction_account (account_id),
    KEY idx_transaction_type (type),
    KEY idx_transaction_occurred (occurred_at),
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES bank_accounts (id),
    CONSTRAINT fk_transactions_transfer FOREIGN KEY (transfer_id) REFERENCES transfers (id)
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    actor_email VARCHAR(160) NOT NULL,
    action VARCHAR(80) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id VARCHAR(80),
    details VARCHAR(500),
    ip_address VARCHAR(80),
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_audit_created (created_at)
);

INSERT IGNORE INTO roles (id, name) VALUES
    (1, 'ROLE_CUSTOMER'),
    (2, 'ROLE_ADMIN');
