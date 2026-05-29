package com.soundarya.aitriage.ai;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketAiTriageRepository extends JpaRepository<TicketAiTriage, Long> {

    List<TicketAiTriage> findByTicketIdOrderByCreatedAtDesc(Long ticketId);
}