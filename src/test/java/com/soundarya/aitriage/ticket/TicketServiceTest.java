package com.soundarya.aitriage.ticket;

import com.soundarya.aitriage.ai.AiTriageService;
import com.soundarya.aitriage.audit.AuditLogService;
import com.soundarya.aitriage.embedding.TicketEmbeddingService;
import com.soundarya.aitriage.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private AiTriageService aiTriageService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private TicketEmbeddingService ticketEmbeddingService;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void createTicket_shouldSaveTicketAndCreateEmbedding() {
        CreateTicketRequest request = new CreateTicketRequest(
                "Payment service timeout after deployment",
                "Users are getting 504 gateway timeout errors after deployment.",
                "prod",
                "SERVICE_NOW"
        );

        Ticket savedTicket = new Ticket();
        savedTicket.setTitle(request.title());
        savedTicket.setDescription(request.description());
        savedTicket.setEnvironment("PROD");
        savedTicket.setSource(request.source());
        savedTicket.setStatus(TicketStatus.NEW);

        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        TicketResponse response = ticketService.createTicket(request);

        assertEquals("Payment service timeout after deployment", response.title());
        assertEquals("PROD", response.environment());
        assertEquals(TicketStatus.NEW, response.status());

        verify(ticketRepository, times(1)).save(any(Ticket.class));
        verify(ticketEmbeddingService, times(1)).createOrUpdateEmbedding(any(Ticket.class));
    }

    @Test
    void getTicketById_whenTicketDoesNotExist_shouldThrowResourceNotFoundException() {
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> ticketService.getTicketById(999L)
        );

        assertEquals("Ticket not found with id: 999", exception.getMessage());
        verify(ticketRepository, times(1)).findById(999L);
    }
}