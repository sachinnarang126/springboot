-- Create token_blacklist table for logout functionality
-- This table stores invalidated JWT tokens until they expire

CREATE TABLE IF NOT EXISTS token_blacklist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(512) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL,
    blacklisted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expiration_time TIMESTAMP NOT NULL,
    INDEX idx_token (token),
    INDEX idx_expiration (expiration_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add comments to columns for documentation
ALTER TABLE token_blacklist 
    MODIFY COLUMN id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
    MODIFY COLUMN token VARCHAR(512) NOT NULL UNIQUE COMMENT 'The JWT token that has been invalidated',
    MODIFY COLUMN username VARCHAR(255) NOT NULL COMMENT 'Username associated with the token',
    MODIFY COLUMN blacklisted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'When the token was blacklisted',
    MODIFY COLUMN expiration_time TIMESTAMP NOT NULL COMMENT 'When the token expires naturally';

