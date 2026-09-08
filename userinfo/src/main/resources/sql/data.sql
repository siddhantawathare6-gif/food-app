INSERT IGNORE INTO userdb.role (id, name) VALUES
(1,'ROLE_ADMIN'),
(2,'ROLE_USER');

INSERT IGNORE INTO userdb.user (id, username, password, email) VALUES
(1,'admin','$2a$10$PJzlkj0ZIlNlxq5ijFj9F.0HM2OBcB37gkbhHx5fjms3w5FQnvg5W','admin@gmail.com');

INSERT IGNORE INTO userdb.user_roles (user_id, role_id) VALUES
(1, 1);