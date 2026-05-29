CREATE TABLE ticket_audit_logs (
                                   id BIGSERIAL PRIMARY KEY,
                                   ticket_id BIGINT NOT NULL REFERENCES tickets(id),
                                   action VARCHAR(100) NOT NULL,
                                   old_value TEXT,
                                   new_value TEXT,
                                   reason TEXT,
                                   created_by VARCHAR(100) NOT NULL DEFAULT 'system',
                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ticket_audit_logs_ticket_id ON ticket_audit_logs(ticket_id);
CREATE INDEX idx_ticket_audit_logs_action ON ticket_audit_logs(action);
CREATE INDEX idx_ticket_audit_logs_created_at ON ticket_audit_logs(created_at);