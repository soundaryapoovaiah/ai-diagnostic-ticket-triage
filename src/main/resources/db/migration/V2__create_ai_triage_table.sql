CREATE TABLE ticket_ai_triage (
                                  id BIGSERIAL PRIMARY KEY,
                                  ticket_id BIGINT NOT NULL REFERENCES tickets(id),
                                  model_name VARCHAR(100) NOT NULL,
                                  prompt_version VARCHAR(50) NOT NULL,
                                  confidence_score NUMERIC(5,2),
                                  root_cause_hypothesis TEXT,
                                  suggested_fix TEXT,
                                  raw_ai_response TEXT,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ticket_ai_triage_ticket_id ON ticket_ai_triage(ticket_id);
CREATE INDEX idx_ticket_ai_triage_model_name ON ticket_ai_triage(model_name);