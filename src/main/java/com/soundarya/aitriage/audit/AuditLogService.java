package com.soundarya.aitriage.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    private final TicketAuditLogRepository ticketAuditLogRepository;

    public AuditLogService(TicketAuditLogRepository ticketAuditLogRepository) {
        this.ticketAuditLogRepository = ticketAuditLogRepository;
    }

    @Transactional
    public void logAction(
            Long ticketId,
            AuditAction action,
            String oldValue,
            String newValue,
            String reason,
            String createdBy
    ) {
        TicketAuditLog auditLog = new TicketAuditLog();
        auditLog.setTicketId(ticketId);
        auditLog.setAction(action);
        auditLog.setOldValue(oldValue);
        auditLog.setNewValue(newValue);
        auditLog.setReason(reason);
        auditLog.setCreatedBy(createdBy);

        ticketAuditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public List<TicketAuditResponse> getAuditLogs(Long ticketId) {
        return ticketAuditLogRepository.findByTicketIdOrderByCreatedAtDesc(ticketId)
                .stream()
                .map(TicketAuditResponse::fromEntity)
                .toList();
    }
}