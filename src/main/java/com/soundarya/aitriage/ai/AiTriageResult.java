package com.soundarya.aitriage.ai;

import com.soundarya.aitriage.ticket.Severity;
import com.soundarya.aitriage.ticket.TicketCategory;

public record AiTriageResult(
        TicketCategory category,
        Severity severity,
        String assignedTeam,
        String rootCauseHypothesis,
        String suggestedFix,
        double confidenceScore,
        String rawAiResponse
) {
}