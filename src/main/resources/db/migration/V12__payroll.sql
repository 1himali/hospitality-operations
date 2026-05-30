CREATE TABLE IF NOT EXISTS payroll_employees (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(200)    NOT NULL,
    position        VARCHAR(100)    NOT NULL,
    department      VARCHAR(100)    NOT NULL,
    salary          NUMERIC(12, 2)  NOT NULL,
    status          VARCHAR(30)     NOT NULL DEFAULT 'ACTIVE',
    phone           VARCHAR(20),
    email           VARCHAR(200),
    hire_date       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    tenant_schema   VARCHAR(100)    NOT NULL DEFAULT 'default',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
