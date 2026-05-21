CREATE SEQUENCE IF NOT EXISTS menu_sequence START WITH 10000 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS category_type
(
    id          BIGINT PRIMARY KEY DEFAULT nextval('menu_sequence'),
    type_name   VARCHAR(128),
    description VARCHAR(500),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS category
(
    id          BIGINT PRIMARY KEY DEFAULT nextval('menu_sequence'),
    name        VARCHAR(128),
    description VARCHAR(500),
    parent_id   BIGINT,
    type_id     BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS password_view
(
    id             BIGSERIAL PRIMARY KEY,
    business_id    VARCHAR(64),
    name           VARCHAR(128),
    password       VARCHAR(256),
    description    VARCHAR(500),
    account_number VARCHAR(128),
    websit         VARCHAR(256),
    like_status    BOOLEAN   NOT NULL DEFAULT FALSE,
    category       INTEGER,
    last_view_time TIMESTAMP,
    deleted        BOOLEAN   NOT NULL DEFAULT FALSE,
    create_time    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version        INTEGER   NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS password_view_business_id_idx
    ON password_view (business_id)
    WHERE business_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS password_view_deleted_idx
    ON password_view (deleted);
