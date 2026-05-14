-- V4__create_idempotency_keys_table.sql

CREATE TABLE  idempotency_keys(
    id BIGSERIAL PRIMARY KEY ,
    user_id BIGINT NOT NULL ,
    idempotency_key VARCHAR(100) NOT NULL ,
    request_path VARCHAR(255) NOT NULL ,
    response_status INT ,
    response_body JSONB ,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW() ,
    CONSTRAINT fk_idempotency_user FOREIGN KEY (user_id) REFERENCES users(id) ,
    CONSTRAINT fk_user_idempotency_key UNIQUE (user_id , idempotency_key)
);

-- Index for cleaner to quickly delete old keys
CREATE INDEX  idx_idempotency_created_at ON idempotency_keys(created_at);