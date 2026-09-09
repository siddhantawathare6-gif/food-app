-- === uk_username ===
SET @exists := (SELECT COUNT(*) FROM information_schema.statistics
                 WHERE table_schema = DATABASE() AND table_name = 'user' AND index_name = 'uk_username');
SET @sql := IF(@exists > 0, 'ALTER TABLE user DROP INDEX uk_username', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- === uk_email ===
SET @exists := (SELECT COUNT(*) FROM information_schema.statistics
                 WHERE table_schema = DATABASE() AND table_name = 'user' AND index_name = 'uk_email');
SET @sql := IF(@exists > 0, 'ALTER TABLE user DROP INDEX uk_email', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- === uk_mobile_number ===
SET @exists := (SELECT COUNT(*) FROM information_schema.statistics
                 WHERE table_schema = DATABASE() AND table_name = 'user' AND index_name = 'uk_mobile_number');
SET @sql := IF(@exists > 0, 'ALTER TABLE user DROP INDEX uk_mobile_number', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE userdb.user ADD CONSTRAINT uk_username UNIQUE (username);
ALTER TABLE userdb.user ADD CONSTRAINT uk_email UNIQUE (email);
ALTER TABLE userdb.user ADD CONSTRAINT uk_mobile_number UNIQUE (mobile_number);

INSERT IGNORE INTO userdb.role (id, name) VALUES
(1,'ROLE_ADMIN'),
(2,'ROLE_USER');

INSERT IGNORE INTO userdb.user (id, username, password, email, mobile_number) VALUES
(1,'admin','$2a$10$PJzlkj0ZIlNlxq5ijFj9F.0HM2OBcB37gkbhHx5fjms3w5FQnvg5W','admin@gmail.com', '1234567890');

INSERT IGNORE INTO userdb.user_roles (user_id, role_id) VALUES
(1, 1);