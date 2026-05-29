package com.soundarya.aitriage.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @NotBlank(message = "Description is required")
        String description,

        @NotBlank(message = "Environment is required")
        @Size(max = 30, message = "Environment must not exceed 30 characters")
        String environment,

        @Size(max = 50, message = "Source must not exceed 50 characters")
        String source
) {
}