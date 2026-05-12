ALTER TABLE transactions ALTER COLUMN destination_wallet_id DROP NOT NULL;
ALTER TABLE transactions ALTER COLUMN source_wallet_id DROP NOT NULL;
UPDATE wallets SET wallet_status = 'ACTIVE';
UPDATE users SET user_role = 'ADMIN' WHERE id = 2;