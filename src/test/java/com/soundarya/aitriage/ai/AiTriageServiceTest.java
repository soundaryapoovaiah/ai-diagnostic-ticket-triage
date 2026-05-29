package com.soundarya.aitriage.ai;

import com.soundarya.aitriage.ticket.Severity;
import com.soundarya.aitriage.ticket.Ticket;
import com.soundarya.aitriage.ticket.TicketCategory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiTriageServiceTest {

    @Mock
    private TicketAiTriageRepository ticketAiTriageRepository;

    @Mock
    private OpenAiTriageClient openAiTriageClient;

    @Test
    void triage_whenOpenAiFails_shouldUseRuleBasedFallback() {
        AiTriageService aiTriageService = new AiTriageService(
                ticketAiTriageRepository,
                openAiTriageClient
        );

        Ticket ticket = new Ticket();
        ticket.setTitle("Payment service timeout after deployment");
        ticket.setDescription("Users are getting 504 errors. Logs show Hikari connection pool exhausted.");
        ticket.setEnvironment("PROD");
        ticket.setSource("SERVICE_NOW");

        when(openAiTriageClient.triage(ticket))
                .thenThrow(new IllegalStateException("429 Too Many Requests"));

        AiTriageResult result = aiTriageService.triage(ticket);

        assertEquals(Severity.CRITICAL, result.severity());
        assertEquals(TicketCategory.DATABASE_CONNECTIVITY, result.category());
        assertEquals("PLATFORM_DB_TEAM", result.assignedTeam());

        ArgumentCaptor<TicketAiTriage> captor = ArgumentCaptor.forClass(TicketAiTriage.class);
        verify(ticketAiTriageRepository).save(captor.capture());

        TicketAiTriage savedRecord = captor.getValue();

        assertEquals("RULE_BASED_TRIAGE_V1", savedRecord.getModelName());
        assertEquals("fallback-v1", savedRecord.getPromptVersion());
        assertEquals(BigDecimal.valueOf(0.86), savedRecord.getConfidenceScore());
    }
}