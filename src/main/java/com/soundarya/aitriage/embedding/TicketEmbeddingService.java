package com.soundarya.aitriage.embedding;

import com.soundarya.aitriage.exception.ResourceNotFoundException;
import com.soundarya.aitriage.ticket.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

@Service
public class TicketEmbeddingService {

    private final JdbcTemplate jdbcTemplate;
    private final TicketRepository ticketRepository;
    private final LocalTicketEmbeddingProvider embeddingProvider;

    public TicketEmbeddingService(
            JdbcTemplate jdbcTemplate,
            TicketRepository ticketRepository,
            LocalTicketEmbeddingProvider embeddingProvider
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.ticketRepository = ticketRepository;
        this.embeddingProvider = embeddingProvider;
    }

    @Transactional
    public void createOrUpdateEmbedding(Ticket ticket) {
        double[] embedding = embeddingProvider.generateEmbedding(ticket);
        String pgVector = toPgVector(embedding);
        String contentHash = sha256(ticket.getTitle() + "|" + ticket.getDescription());

        jdbcTemplate.update("""
                INSERT INTO ticket_embeddings(ticket_id, embedding, content_hash)
                VALUES (?, CAST(? AS vector), ?)
                ON CONFLICT (ticket_id)
                DO UPDATE SET
                    embedding = EXCLUDED.embedding,
                    content_hash = EXCLUDED.content_hash,
                    created_at = CURRENT_TIMESTAMP
                """,
                ticket.getId(),
                pgVector,
                contentHash
        );
    }

    @Transactional
    public int backfillEmbeddings() {
        List<Ticket> tickets = ticketRepository.findAll();

        for (Ticket ticket : tickets) {
            createOrUpdateEmbedding(ticket);
        }

        return tickets.size();
    }

    @Transactional(readOnly = true)
    public List<SimilarTicketResponse> findSimilarTickets(Long ticketId, int limit) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));

        double[] queryEmbedding = embeddingProvider.generateEmbedding(ticket);
        String queryVector = toPgVector(queryEmbedding);

        return jdbcTemplate.query("""
                SELECT
                    t.id AS ticket_id,
                    t.title,
                    t.status,
                    t.severity,
                    t.category,
                    t.assigned_team,
                    te.embedding <-> CAST(? AS vector) AS distance
                FROM ticket_embeddings te
                JOIN tickets t ON t.id = te.ticket_id
                WHERE t.id <> ?
                ORDER BY te.embedding <-> CAST(? AS vector)
                LIMIT ?
                """,
                (rs, rowNum) -> {
                    double distance = rs.getDouble("distance");
                    double similarityScore = 1.0 / (1.0 + distance);

                    return new SimilarTicketResponse(
                            rs.getLong("ticket_id"),
                            rs.getString("title"),
                            parseEnum(TicketStatus.class, rs.getString("status")),
                            parseEnum(Severity.class, rs.getString("severity")),
                            parseEnum(TicketCategory.class, rs.getString("category")),
                            rs.getString("assigned_team"),
                            Math.round(similarityScore * 100.0) / 100.0
                    );
                },
                queryVector,
                ticketId,
                queryVector,
                limit
        );
    }

    private String toPgVector(double[] vector) {
        StringBuilder builder = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            builder.append(vector[i]);
            if (i < vector.length - 1) {
                builder.append(",");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to generate content hash", ex);
        }
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Enum.valueOf(enumClass, value);
    }
}