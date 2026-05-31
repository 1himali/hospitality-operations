CREATE TABLE IF NOT EXISTS activity_logs (
    id              BIGSERIAL PRIMARY KEY,
    actor           VARCHAR(100)    NOT NULL,
    role            VARCHAR(50)     NOT NULL,
    action_type     VARCHAR(30)     NOT NULL,
    module          VARCHAR(50)     NOT NULL,
    entity_id       BIGINT,
    before_state    TEXT,
    after_state     TEXT,
    occurred_at     TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    success         BOOLEAN         NOT NULL DEFAULT TRUE,
    notes           TEXT
);

CREATE INDEX idx_activity_actor       ON activity_logs (actor);
CREATE INDEX idx_activity_module      ON activity_logs (module);
CREATE INDEX idx_activity_action_type ON activity_logs (action_type);
CREATE INDEX idx_activity_occurred_at ON activity_logs (occurred_at);
