-- Rooms table (primary CRUD entity)
CREATE TABLE IF NOT EXISTS rooms (
    id              BIGSERIAL PRIMARY KEY,
    room_number     VARCHAR(10)     NOT NULL UNIQUE,
    type            VARCHAR(50)     NOT NULL,
    floor           INTEGER         NOT NULL,
    status          VARCHAR(30)     NOT NULL DEFAULT 'VACANT',
    rate_per_night  NUMERIC(10, 2)  NOT NULL,
    image_url       VARCHAR(500),
    issue_notes     VARCHAR(500),
    tenant_schema   VARCHAR(100)    NOT NULL DEFAULT 'default',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- API usage logs table
CREATE TABLE IF NOT EXISTS api_usage_logs (
    id              BIGSERIAL PRIMARY KEY,
    endpoint        VARCHAR(200)    NOT NULL,
    http_method     VARCHAR(10)     NOT NULL,
    status_code     INTEGER,
    duration_ms     BIGINT,
    tenant_schema   VARCHAR(100),
    called_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- Users table for JWT auth
CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(100)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    role            VARCHAR(50)     NOT NULL DEFAULT 'ROLE_USER',
    tenant_schema   VARCHAR(100)    NOT NULL DEFAULT 'default',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);