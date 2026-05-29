package com.soundarya.aitriage.health;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HealthCheckControllerTest {

    @Test
    void healthCheck_shouldReturnUpStatus() {
        HealthCheckController controller = new HealthCheckController();

        Map<String, String> response = controller.healthCheck();

        assertEquals("UP", response.get("status"));
        assertEquals("AI Diagnostic Ticket Triage API", response.get("service"));
    }
}