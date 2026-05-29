CREATE TABLE tickets (
                         id BIGSERIAL PRIMARY KEY,
                         title VARCHAR(255) NOT NULL,
                         description TEXT NOT NULL,
                         environment VARCHAR(30) NOT NULL,
                         source VARCHAR(50),
                         status VARCHAR(50) NOT NULL DEFAULT 'NEW',
                         severity VARCHAR(50),
                         category VARCHAR(100),
                         assigned_team VARCHAR(100),
                         resolution_summary TEXT,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP
);

CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_severity ON tickets(severity);
CREATE INDEX idx_tickets_category ON tickets(category);
CREATE INDEX idx_tickets_assigned_team ON tickets(assigned_team);