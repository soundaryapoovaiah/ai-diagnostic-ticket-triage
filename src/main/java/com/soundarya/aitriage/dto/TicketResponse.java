package com.soundarya.aitriage.ticket;

import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        String title,
        String description,
        String environment,
        String source,
        TicketStatus status,
        Severity severity,
        TicketCategory category,
        String assignedTeam,
        String resolutionSummary,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TicketResponse fromEntity(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getEnvironment(),
                ticket.getSource(),
                ticket.getStatus(),
                ticket.getSeverity(),
                ticket.getCategory(),
                ticket.getAssignedTeam(),
                ticket.getResolutionSummary(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }
}