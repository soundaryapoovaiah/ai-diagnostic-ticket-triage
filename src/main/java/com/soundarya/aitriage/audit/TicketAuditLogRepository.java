package com.soundarya.aitriage.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketAuditLogRepository extends JpaRepository<TicketAuditLog, Long> {

    List<TicketAuditLog> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
}