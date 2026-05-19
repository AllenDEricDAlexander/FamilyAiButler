INSERT INTO category_type (id, type_name, description, create_time, update_time)
SELECT 1, 'account', '账号分类', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM category_type WHERE id = 1);

INSERT INTO category (id, name, description, parent_id, type_id, create_time, update_time)
SELECT 1, 'Web', 'Web 账号', 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM category WHERE id = 1);

INSERT INTO category (id, name, description, parent_id, type_id, create_time, update_time)
SELECT 2, 'App', 'App 账号', 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM category WHERE id = 2);

INSERT INTO category (id, name, description, parent_id, type_id, create_time, update_time)
SELECT 3, 'Email', '邮箱账号', 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM category WHERE id = 3);
