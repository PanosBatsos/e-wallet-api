-- V2__add_index_to_unprocessed_outbox_events.sql

CREATE INDEX idx_outbox_unprocessed ON outbox_events(created_at ASC) WHERE processed = false;