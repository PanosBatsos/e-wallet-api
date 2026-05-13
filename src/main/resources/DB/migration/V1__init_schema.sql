-- V1__init_schema.sql
-- Initial database schema setup for application

-- Users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY ,
    first_name VARCHAR(255) NOT NULL ,
    last_name VARCHAR(255) NOT NULL ,
    email VARCHAR(255) NOT NULL UNIQUE ,
    password VARCHAR(255) NOT NULL ,
    tax_number VARCHAR(15) NOT NULL UNIQUE ,
    id_card_number VARCHAR(15) NOT NULL UNIQUE ,
    birth_date DATE NOT NULL ,
    user_role VARCHAR(30) NOT NULL ,
    created_at TIMESTAMP WITHOUT TIME ZONE
);

-- Wallets
CREATE TABLE  wallets(
    id BIGSERIAL PRIMARY KEY ,
    balance NUMERIC(19 , 2) NOT NULL ,
    currency VARCHAR(5) NOT NULL ,
    wallet_status VARCHAR(30) ,
    version BIGINT NOT NULL DEFAULT 0,
    user_id BIGINT NOT NULL UNIQUE ,
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Transactions
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY ,
    amount NUMERIC(19 , 2) NOT NULL ,
    description VARCHAR(255) ,
    status VARCHAR(30) NOT NULL ,
    type VARCHAR(30) NOT NULL ,
    timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    source_wallet_id BIGINT ,
    destination_wallet_id BIGINT ,
    CONSTRAINT fk_source_wallet FOREIGN KEY (source_wallet_id) REFERENCES wallets(id),
    CONSTRAINT fk_dest_wallet FOREIGN KEY (destination_wallet_id) REFERENCES wallets(id)
);

-- Wallet indexes
CREATE INDEX idx_source_wallet_type ON transactions(source_wallet_id , type);
CREATE INDEX idx_dest_wallet_type ON transactions(destination_wallet_id , type);

-- Refresh Tokens
CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY ,
    token VARCHAR(300) NOT NULL ,
    expiry_date TIMESTAMP WITHOUT TIME ZONE NOT NULL ,
    user_id BIGINT ,
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Wallet indexes
CREATE INDEX idx_refresh_token_expiry ON refresh_tokens(expiry_date);

-- Outbox Events
CREATE TABLE outbox_events (
    id BIGSERIAL PRIMARY KEY ,
    aggregate_type VARCHAR(255) NOT NULL ,
    aggregate_id VARCHAR(255) NOT NULL ,
    payload TEXT NOT NULL ,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE
);

-- Processed Events
CREATE TABLE processed_events(
    event_id VARCHAR(256) PRIMARY KEY ,
    processed_at TIMESTAMP WITHOUT TIME ZONE
);