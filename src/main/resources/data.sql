-- Default seed users (password: admin123)
INSERT INTO users (username, email, password, role, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at)
VALUES
  ('admin',   'admin@spareparts.com',   '$2b$10$g0Ye1zb8XWGGgheyJJ51LOfKwUofear2mPg0CSlIgfrr2gm75K1Nm', 'ADMIN',   true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('manager', 'manager@spareparts.com', '$2b$10$g0Ye1zb8XWGGgheyJJ51LOfKwUofear2mPg0CSlIgfrr2gm75K1Nm', 'MANAGER', true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('user',    'user@spareparts.com',    '$2b$10$g0Ye1zb8XWGGgheyJJ51LOfKwUofear2mPg0CSlIgfrr2gm75K1Nm', 'USER',    true, true, true, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
