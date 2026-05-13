-- V3__add_index_for_cleanup_job.sql
-- Create a partial index for the nightly cleanup job to find processed events instantly
CREATE INDEX idx_outbox_processed ON outbox_events(created_at ASC) WHERE processed = true;