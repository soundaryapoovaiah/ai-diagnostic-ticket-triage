package com.soundarya.aitriage.embedding;

import com.soundarya.aitriage.ticket.Ticket;
import org.springframework.stereotype.Component;

@Component
public class LocalTicketEmbeddingProvider {

    public double[] generateEmbedding(Ticket ticket) {
        String text = (
                ticket.getTitle() + " " +
                        ticket.getDescription() + " " +
                        ticket.getEnvironment()
        ).toLowerCase();

        double[] vector = new double[8];

        addIfContains(vector, 0, text, "database", "db", "hikari", "connection pool", "sql");
        addIfContains(vector, 1, text, "api", "endpoint", "gateway", "504", "timeout");
        addIfContains(vector, 2, text, "deployment", "release", "rollback", "version");
        addIfContains(vector, 3, text, "login", "auth", "token", "sso", "certificate");
        addIfContains(vector, 4, text, "network", "dns", "firewall", "latency", "routing");
        addIfContains(vector, 5, text, "slow", "performance", "cpu", "memory", "jvm");
        addIfContains(vector, 6, text, "prod", "production", "critical", "down", "data loss");
        addIfContains(vector, 7, text, "payment", "checkout", "order", "billing", "transaction");

        normalize(vector);
        return vector;
    }

    private void addIfContains(double[] vector, int index, String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                vector[index] += 1.0;
            }
        }
    }

    private void normalize(double[] vector) {
        double sumSquares = 0.0;

        for (double value : vector) {
            sumSquares += value * value;
        }

        double norm = Math.sqrt(sumSquares);

        if (norm == 0.0) {
            vector[7] = 1.0;
            return;
        }

        for (int i = 0; i < vector.length; i++) {
            vector[i] = vector[i] / norm;
        }
    }
}