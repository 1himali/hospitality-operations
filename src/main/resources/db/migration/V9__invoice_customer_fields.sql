ALTER TABLE bills ADD COLUMN IF NOT EXISTS customer_name VARCHAR(200);
ALTER TABLE bills ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20);
ALTER TABLE bills ADD COLUMN IF NOT EXISTS email VARCHAR(200);
ALTER TABLE bills ADD COLUMN IF NOT EXISTS invoice_number VARCHAR(30);
ALTER TABLE bills ADD COLUMN IF NOT EXISTS status VARCHAR(30) NOT NULL DEFAULT 'PAID';
ALTER TABLE bills ADD COLUMN IF NOT EXISTS notes TEXT;

CREATE TABLE IF NOT EXISTS bill_line_items (
    id              BIGSERIAL PRIMARY KEY,
    bill_id         BIGINT NOT NULL REFERENCES bills(id) ON DELETE CASCADE,
    item_type       VARCHAR(30) NOT NULL,
    item_id         BIGINT,
    description     VARCHAR(300) NOT NULL,
    quantity        INTEGER NOT NULL DEFAULT 1,
    unit_price      NUMERIC(10, 2) NOT NULL,
    total_price     NUMERIC(10, 2) NOT NULL,
    tenant_schema   VARCHAR(100) NOT NULL DEFAULT 'default'
);
