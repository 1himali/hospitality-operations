CREATE TABLE IF NOT EXISTS menu_items (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(150)    NOT NULL,
    category        VARCHAR(50)     NOT NULL,
    price           NUMERIC(10, 2)  NOT NULL,
    available       BOOLEAN         NOT NULL DEFAULT true,
    tenant_schema   VARCHAR(100)    NOT NULL DEFAULT 'default',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS dining_tables (
    id                  BIGSERIAL PRIMARY KEY,
    table_number        VARCHAR(10)     NOT NULL UNIQUE,
    capacity            INTEGER         NOT NULL,
    status              VARCHAR(30)     NOT NULL DEFAULT 'AVAILABLE',
    table_type          VARCHAR(30)     NOT NULL DEFAULT 'DINING',
    location            VARCHAR(100),
    seated_at           TIMESTAMPTZ,
    current_order_id    BIGINT,
    reservation_name    VARCHAR(100),
    party_size          INTEGER,
    eta_minutes         INTEGER,
    tenant_schema       VARCHAR(100)    NOT NULL DEFAULT 'default',
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS restaurant_orders (
    id              BIGSERIAL PRIMARY KEY,
    order_reference VARCHAR(20)     UNIQUE,
    table_id        BIGINT          REFERENCES dining_tables(id),
    status          VARCHAR(30)     NOT NULL DEFAULT 'NEW',
    total_amount    NUMERIC(10, 2),
    tenant_schema   VARCHAR(100)    NOT NULL DEFAULT 'default',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS order_items (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT          NOT NULL REFERENCES restaurant_orders(id),
    menu_item_id    BIGINT          NOT NULL REFERENCES menu_items(id),
    quantity        INTEGER         NOT NULL,
    unit_price      NUMERIC(10, 2)  NOT NULL,
    subtotal        NUMERIC(10, 2)  NOT NULL
);

CREATE TABLE IF NOT EXISTS bills (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT          NOT NULL REFERENCES restaurant_orders(id),
    order_reference VARCHAR(20),
    table_id        BIGINT          REFERENCES dining_tables(id),
    server_name     VARCHAR(100),
    subtotal        NUMERIC(10, 2)  NOT NULL,
    tax_rate        NUMERIC(5, 4)   NOT NULL DEFAULT 0.08875,
    tax_amount      NUMERIC(10, 2)  NOT NULL,
    discount        NUMERIC(10, 2)  NOT NULL DEFAULT 0.00,
    total_due       NUMERIC(10, 2)  NOT NULL,
    ai_description  TEXT,
    tenant_schema   VARCHAR(100)    NOT NULL DEFAULT 'default',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);