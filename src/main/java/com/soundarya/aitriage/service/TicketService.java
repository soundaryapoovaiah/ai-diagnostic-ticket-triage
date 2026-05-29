package com.soundarya.aitriage.ticket;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.soundarya.aitriage.exception.ResourceNotFoundException;
import com.soundarya.aitriage.ai.AiTriageResult;
import com.soundarya.aitriage.ai.AiTriageService;
import com.soundarya.aitriage.audit.AuditAction;
import com.soundarya.aitriage.audit.AuditLogService;
import com.soundarya.aitriage.audit.TicketAuditResponse;
import com.soundarya.aitriage.embedding.SimilarTicketResponse;
import com.soundarya.aitriage.embedding.TicketEmbeddingService;
import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final AiTriageService aiTriageService;
    private final AuditLogService auditLogService;
    private final TicketEmbeddingService ticketEmbeddingService;


    public TicketService(TicketRepository ticketRepository, AiTriageService aiTriageService, AuditLogService auditLogService,TicketEmbeddingService ticketEmbeddingService ) {
        this.ticketRepository = ticketRepository;
        this.aiTriageService = aiTriageService;
        this.auditLogService = auditLogService;
        this.ticketEmbeddingService = ticketEmbeddingService;
    }

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setEnvironment(request.environment().toUpperCase());
        ticket.setSource(request.source());
        ticket.setStatus(TicketStatus.NEW);

        Ticket savedTicket = ticketRepository.save(ticket);
        ticketEmbeddingService.createOrUpdateEmbedding(savedTicket);
        return TicketResponse.fromEntity(savedTicket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll()
                .stream()
                .map(TicketResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        return TicketResponse.fromEntity(ticket);
    }
    @Transactional
    public TicketResponse triageTicket(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
        String oldValue = formatTicketDecision(ticket);

        AiTriageResult result = aiTriageService.triage(ticket);

        ticket.setStatus(TicketStatus.TRIAGED);
        ticket.setSeverity(result.severity());
        ticket.setCategory(result.category());
        ticket.setAssignedTeam(result.assignedTeam());

        Ticket savedTicket = ticketRepository.save(ticket);

        String newValue = formatTicketDecision(savedTicket);

        auditLogService.logAction(
                savedTicket.getId(),
                AuditAction.AI_TRIAGE_COMPLETED,
                oldValue,
                newValue,
                "Ticket triaged using AI workflow with fallback support.",
                "system"
        );

        return TicketResponse.fromEntity(savedTicket);
    }
    @Transactional
    public TicketResponse overrideTicket(Long id, OverrideTicketRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        String oldValue = formatTicketDecision(ticket);

        ticket.setStatus(TicketStatus.TRIAGED);
        ticket.setSeverity(request.severity());
        ticket.setAssignedTeam(request.assignedTeam());

        if (request.category() != null) {
            ticket.setCategory(request.category());
        }

        Ticket savedTicket = ticketRepository.save(ticket);

        String newValue = formatTicketDecision(savedTicket);

        auditLogService.logAction(
                savedTicket.getId(),
                AuditAction.HUMAN_OVERRIDE,
                oldValue,
                newValue,
                request.overrideReason(),
                "manual-user"
        );

        return TicketResponse.fromEntity(savedTicket);
    }
    private String formatTicketDecision(Ticket ticket) {
        return "status=" + ticket.getStatus()
                + ", severity=" + ticket.getSeverity()
                + ", category=" + ticket.getCategory()
                + ", assignedTeam=" + ticket.getAssignedTeam();
    }
    @Transactional(readOnly = true)
    public List<TicketAuditResponse> getTicketAuditLogs(Long id) {
        ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));

        return auditLogService.getAuditLogs(id);
    }
    @Transactional
    public int backfillTicketEmbeddings() {
        return ticketEmbeddingService.backfillEmbeddings();
    }

    @Transactional(readOnly = true)
    public List<SimilarTicketResponse> getSimilarTickets(Long id) {
        return ticketEmbeddingService.findSimilarTickets(id, 5);
    }
}