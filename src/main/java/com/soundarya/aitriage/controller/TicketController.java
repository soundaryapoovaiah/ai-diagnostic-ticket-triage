package com.soundarya.aitriage.ticket;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.soundarya.aitriage.audit.TicketAuditResponse;
import com.soundarya.aitriage.embedding.SimilarTicketResponse;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse createTicket(@Valid @RequestBody CreateTicketRequest request) {
        return ticketService.createTicket(request);
    }

    @GetMapping
    public List<TicketResponse> getAllTickets() {
        return ticketService.getAllTickets();
    }

    @GetMapping("/{id}")
    public TicketResponse getTicketById(@PathVariable Long id) {
        return ticketService.getTicketById(id);
    }
    @PostMapping("/{id}/triage")
    public TicketResponse triageTicket(@PathVariable Long id) {
        return ticketService.triageTicket(id);
    }
    @GetMapping("/{id}/audit")
    public List<TicketAuditResponse> getTicketAuditLogs(@PathVariable Long id) {
        return ticketService.getTicketAuditLogs(id);
    }

    @PatchMapping("/{id}/override")
    public TicketResponse overrideTicket(
            @PathVariable Long id,
            @Valid @RequestBody OverrideTicketRequest request
    ) {
        return ticketService.overrideTicket(id, request);
    }

    @PostMapping("/embeddings/backfill")
    public Map<String, Object> backfillTicketEmbeddings() {
        int count = ticketService.backfillTicketEmbeddings();

        return Map.of(
                "status", "COMPLETED",
                "ticketsEmbedded", count
        );
    }

    @GetMapping("/{id}/similar")
    public List<SimilarTicketResponse> getSimilarTickets(@PathVariable Long id) {
        return ticketService.getSimilarTickets(id);
    }
}