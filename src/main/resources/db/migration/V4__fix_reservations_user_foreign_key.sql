-- V4__fix_reservations_user_foreign_key.sql

-- Drop the incorrect foreign key constraint
ALTER TABLE reservations
DROP CONSTRAINT IF EXISTS fk_user;

-- Add the correct foreign key constraint for user_id
ALTER TABLE reservations
ADD CONSTRAINT fk_user
    FOREIGN KEY (user_id) REFERENCES users(id);
