package com.soundarya.aitriage.audit;

import java.time.LocalDateTime;

public record TicketAuditResponse(
        Long id,
        Long ticketId,
        AuditAction action,
        String oldValue,
        String newValue,
        String reason,
        String createdBy,
        LocalDateTime createdAt
) {
    public static TicketAuditResponse fromEntity(TicketAuditLog auditLog) {
        return new TicketAuditResponse(
                auditLog.getId(),
                auditLog.getTicketId(),
                auditLog.getAction(),
                auditLog.getOldValue(),
                auditLog.getNewValue(),
                auditLog.getReason(),
                auditLog.getCreatedBy(),
                auditLog.getCreatedAt()
        );
    }
}