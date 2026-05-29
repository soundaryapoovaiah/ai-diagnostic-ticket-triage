package com.soundarya.aitriage.embedding;

import com.soundarya.aitriage.ticket.Severity;
import com.soundarya.aitriage.ticket.TicketCategory;
import com.soundarya.aitriage.ticket.TicketStatus;

public record SimilarTicketResponse(
        Long ticketId,
        String title,
        TicketStatus status,
        Severity severity,
        TicketCategory category,
        String assignedTeam,
        double similarityScore
) {
}