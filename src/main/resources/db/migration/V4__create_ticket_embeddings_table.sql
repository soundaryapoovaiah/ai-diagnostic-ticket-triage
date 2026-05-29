CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE ticket_embeddings (
                                   id BIGSERIAL PRIMARY KEY,
                                   ticket_id BIGINT NOT NULL UNIQUE REFERENCES tickets(id),
                                   embedding vector(8) NOT NULL,
                                   content_hash VARCHAR(128) NOT NULL,
                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ticket_embeddings_ticket_id ON ticket_embeddings(ticket_id);