CREATE TABLE IF NOT EXISTS payroll_records (
    id              BIGSERIAL PRIMARY KEY,
    employee_id     BIGINT          NOT NULL REFERENCES payroll_employees(id) ON DELETE CASCADE,
    base_salary     NUMERIC(12, 2)  NOT NULL,
    bonus           NUMERIC(12, 2)  NOT NULL DEFAULT 0,
    deductions      NUMERIC(12, 2)  NOT NULL DEFAULT 0,
    net_pay         NUMERIC(12, 2)  NOT NULL,
    payment_date    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    notes           TEXT,
    status          VARCHAR(30)     NOT NULL DEFAULT 'PAID',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
