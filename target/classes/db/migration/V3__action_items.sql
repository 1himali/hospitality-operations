CREATE TABLE IF NOT EXISTS action_items (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(200)    NOT NULL,
    description     TEXT,
    category        VARCHAR(50)     NOT NULL,
    status          VARCHAR(20)     NOT NULL DEFAULT 'TODO',
    tenant_schema   VARCHAR(100)    NOT NULL DEFAULT 'default',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMPTZ
);