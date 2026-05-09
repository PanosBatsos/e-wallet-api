ALTER TABLE transactions ALTER COLUMN destination_wallet_id DROP NOT NULL;
ALTER TABLE transactions ALTER COLUMN source_wallet_id DROP NOT NULL;