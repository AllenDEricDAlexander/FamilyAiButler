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
