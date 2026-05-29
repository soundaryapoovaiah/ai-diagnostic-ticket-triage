package com.soundarya.aitriage.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OverrideTicketRequest(

        @NotNull(message = "Severity is required")
        Severity severity,

        TicketCategory category,

        @NotBlank(message = "Assigned team is required")
        @Size(max = 100, message = "Assigned team must not exceed 100 characters")
        String assignedTeam,

        @NotBlank(message = "Override reason is required")
        @Size(max = 500, message = "Override reason must not exceed 500 characters")
        String overrideReason
) {
}