CREATE TABLE IF NOT EXISTS uaa_account
(
    account_id          VARCHAR(64) PRIMARY KEY,
    username            VARCHAR(128),
    email               VARCHAR(256),
    phone               VARCHAR(64),
    account_type        VARCHAR(64) NOT NULL,
    status              VARCHAR(64) NOT NULL,
    auth_version        BIGINT      NOT NULL DEFAULT 1,
    entitlement_version BIGINT      NOT NULL DEFAULT 1,
    session_version     BIGINT      NOT NULL DEFAULT 1,
    risk_version        BIGINT      NOT NULL DEFAULT 1,
    deleted             BOOLEAN     NOT NULL DEFAULT FALSE,
    create_time         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version             INTEGER     NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uaa_account_username_idx
    ON uaa_account (username)
    WHERE username IS NOT NULL AND deleted = FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS uaa_account_email_idx
    ON uaa_account (email)
    WHERE email IS NOT NULL AND deleted = FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS uaa_account_phone_idx
    ON uaa_account (phone)
    WHERE phone IS NOT NULL AND deleted = FALSE;

CREATE TABLE IF NOT EXISTS uaa_credential
(
    credential_id   VARCHAR(128) PRIMARY KEY,
    account_id      VARCHAR(64)  NOT NULL,
    credential_type VARCHAR(64)  NOT NULL,
    credential_hash VARCHAR(256) NOT NULL,
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         INTEGER      NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uaa_credential_account_type_idx
    ON uaa_credential (account_id, credential_type)
    WHERE deleted = FALSE;

CREATE TABLE IF NOT EXISTS uaa_profile
(
    profile_id   VARCHAR(64) PRIMARY KEY,
    account_id   VARCHAR(64) NOT NULL,
    profile_type VARCHAR(64) NOT NULL,
    nickname     VARCHAR(128),
    avatar       VARCHAR(512),
    language     VARCHAR(32),
    region       VARCHAR(32),
    deleted      BOOLEAN     NOT NULL DEFAULT FALSE,
    create_time  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version      INTEGER     NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS uaa_profile_account_idx
    ON uaa_profile (account_id)
    WHERE deleted = FALSE;

CREATE TABLE IF NOT EXISTS uaa_device
(
    device_id   VARCHAR(64) PRIMARY KEY,
    account_id  VARCHAR(64) NOT NULL,
    device_name VARCHAR(128),
    device_type VARCHAR(64) NOT NULL,
    fingerprint VARCHAR(256),
    removed     BOOLEAN     NOT NULL DEFAULT FALSE,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version     INTEGER     NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS uaa_device_account_idx
    ON uaa_device (account_id)
    WHERE deleted = FALSE;

CREATE UNIQUE INDEX IF NOT EXISTS uaa_device_fingerprint_idx
    ON uaa_device (account_id, fingerprint)
    WHERE fingerprint IS NOT NULL AND deleted = FALSE;

CREATE TABLE IF NOT EXISTS uaa_oauth_client
(
    client_id                 VARCHAR(128) PRIMARY KEY,
    client_name               VARCHAR(128) NOT NULL,
    client_secret_hash        VARCHAR(256),
    status                    VARCHAR(64)  NOT NULL,
    grant_types               TEXT         NOT NULL,
    scopes                    TEXT         NOT NULL,
    resource_patterns         TEXT         NOT NULL,
    access_token_ttl_seconds  BIGINT       NOT NULL DEFAULT 300,
    refresh_token_ttl_seconds BIGINT       NOT NULL DEFAULT 2592000,
    deleted                   BOOLEAN      NOT NULL DEFAULT FALSE,
    create_time               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version                   INTEGER      NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS uaa_oauth_client_status_idx
    ON uaa_oauth_client (status)
    WHERE deleted = FALSE;

CREATE TABLE IF NOT EXISTS uaa_role
(
    role_code   VARCHAR(128) PRIMARY KEY,
    role_name   VARCHAR(128) NOT NULL,
    status      VARCHAR(64)  NOT NULL,
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version     INTEGER      NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS uaa_role_status_idx
    ON uaa_role (status)
    WHERE deleted = FALSE;

CREATE TABLE IF NOT EXISTS uaa_permission_resource
(
    resource_code    VARCHAR(128) PRIMARY KEY,
    resource_name    VARCHAR(128) NOT NULL,
    resource_type    VARCHAR(64)  NOT NULL,
    resource_service VARCHAR(128),
    path_pattern     VARCHAR(512),
    action           VARCHAR(64),
    status           VARCHAR(64)  NOT NULL,
    deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    create_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version          INTEGER      NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS uaa_permission_resource_type_idx
    ON uaa_permission_resource (resource_type)
    WHERE deleted = FALSE;

CREATE INDEX IF NOT EXISTS uaa_permission_resource_api_idx
    ON uaa_permission_resource (resource_service, path_pattern, action)
    WHERE resource_type = 'API' AND deleted = FALSE;

CREATE TABLE IF NOT EXISTS uaa_role_resource
(
    role_resource_id VARCHAR(256) PRIMARY KEY,
    role_code        VARCHAR(128) NOT NULL,
    resource_code    VARCHAR(128) NOT NULL,
    deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    create_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version          INTEGER      NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uaa_role_resource_unique_idx
    ON uaa_role_resource (role_code, resource_code)
    WHERE deleted = FALSE;

CREATE INDEX IF NOT EXISTS uaa_role_resource_role_idx
    ON uaa_role_resource (role_code)
    WHERE deleted = FALSE;

CREATE TABLE IF NOT EXISTS uaa_account_role
(
    account_role_id VARCHAR(256) PRIMARY KEY,
    account_id      VARCHAR(64)  NOT NULL,
    role_code       VARCHAR(128) NOT NULL,
    deleted         BOOLEAN      NOT NULL DEFAULT FALSE,
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         INTEGER      NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uaa_account_role_unique_idx
    ON uaa_account_role (account_id, role_code)
    WHERE deleted = FALSE;

CREATE INDEX IF NOT EXISTS uaa_account_role_account_idx
    ON uaa_account_role (account_id)
    WHERE deleted = FALSE;

CREATE TABLE IF NOT EXISTS uaa_auth_session
(
    session_id  VARCHAR(64) PRIMARY KEY,
    account_id  VARCHAR(64) NOT NULL,
    profile_id  VARCHAR(64) NOT NULL,
    device_id   VARCHAR(64) NOT NULL,
    client_id   VARCHAR(128),
    status      VARCHAR(64) NOT NULL,
    deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version     INTEGER     NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS uaa_auth_session_account_idx
    ON uaa_auth_session (account_id)
    WHERE deleted = FALSE;

CREATE INDEX IF NOT EXISTS uaa_auth_session_device_idx
    ON uaa_auth_session (device_id)
    WHERE deleted = FALSE;

CREATE TABLE IF NOT EXISTS uaa_refresh_token
(
    token_id    VARCHAR(64) PRIMARY KEY,
    account_id  VARCHAR(64)  NOT NULL,
    session_id  VARCHAR(64)  NOT NULL,
    device_id   VARCHAR(64)  NOT NULL,
    client_id   VARCHAR(128),
    token_hash  VARCHAR(256) NOT NULL,
    status      VARCHAR(64)  NOT NULL,
    deleted     BOOLEAN      NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version     INTEGER      NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX IF NOT EXISTS uaa_refresh_token_hash_idx
    ON uaa_refresh_token (token_hash)
    WHERE deleted = FALSE;

CREATE INDEX IF NOT EXISTS uaa_refresh_token_account_idx
    ON uaa_refresh_token (account_id)
    WHERE deleted = FALSE;

CREATE INDEX IF NOT EXISTS uaa_refresh_token_session_idx
    ON uaa_refresh_token (session_id)
    WHERE deleted = FALSE;

CREATE INDEX IF NOT EXISTS uaa_refresh_token_device_idx
    ON uaa_refresh_token (device_id)
    WHERE deleted = FALSE;

CREATE TABLE IF NOT EXISTS uaa_access_token
(
    access_token        VARCHAR(128) PRIMARY KEY,
    account_id          VARCHAR(64) NOT NULL,
    profile_id          VARCHAR(64) NOT NULL,
    client_id           VARCHAR(128),
    session_id          VARCHAR(64) NOT NULL,
    device_id           VARCHAR(64) NOT NULL,
    auth_version        BIGINT      NOT NULL DEFAULT 0,
    entitlement_version BIGINT      NOT NULL DEFAULT 0,
    risk_level          VARCHAR(64),
    expires_at          TIMESTAMP   NOT NULL,
    deleted             BOOLEAN     NOT NULL DEFAULT FALSE,
    create_time         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version             INTEGER     NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS uaa_access_token_account_idx
    ON uaa_access_token (account_id)
    WHERE deleted = FALSE;

CREATE INDEX IF NOT EXISTS uaa_access_token_session_idx
    ON uaa_access_token (session_id)
    WHERE deleted = FALSE;

CREATE TABLE IF NOT EXISTS uaa_security_challenge
(
    challenge_id      VARCHAR(128) PRIMARY KEY,
    account_id        VARCHAR(64)  NOT NULL,
    principal         VARCHAR(256) NOT NULL,
    challenge_type    VARCHAR(64)  NOT NULL,
    verification_code VARCHAR(128) NOT NULL,
    used              BOOLEAN      NOT NULL DEFAULT FALSE,
    expires_at        TIMESTAMP    NOT NULL,
    deleted           BOOLEAN      NOT NULL DEFAULT FALSE,
    create_time       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version           INTEGER      NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS uaa_security_challenge_principal_idx
    ON uaa_security_challenge (principal, challenge_type, used)
    WHERE deleted = FALSE;

INSERT INTO uaa_oauth_client (client_id, client_name, client_secret_hash, status, grant_types, scopes,
                              resource_patterns, access_token_ttl_seconds, refresh_token_ttl_seconds, deleted)
VALUES ('family-web', 'Family Web', NULL, 'ACTIVE', 'PASSWORD,REFRESH_TOKEN', 'openid,profile',
        '/**,family-core:/**,family-ai-qwen:/**', 300, 2592000, FALSE)
ON CONFLICT (client_id) DO NOTHING;
