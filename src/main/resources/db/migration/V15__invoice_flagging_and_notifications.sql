-- V15: Add invoice flagging columns and notifications table

ALTER TABLE bills ADD COLUMN flagged BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE bills ADD COLUMN flag_note TEXT;

CREATE TABLE notifications (
    id          BIGSERIAL PRIMARY KEY,
    sender      VARCHAR(100) NOT NULL,
    sender_role VARCHAR(30)  NOT NULL,
    recipient_roles VARCHAR(100) NOT NULL,
    event_type  VARCHAR(50)  NOT NULL,
    entity_type VARCHAR(50)  NOT NULL,
    entity_id   BIGINT,
    message     TEXT,
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notif_recipient_roles ON notifications (recipient_roles);
CREATE INDEX idx_notif_sender ON notifications (sender);
CREATE INDEX idx_notif_created_at ON notifications (created_at);
