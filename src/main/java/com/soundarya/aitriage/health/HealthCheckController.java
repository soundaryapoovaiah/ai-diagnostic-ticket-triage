package com.soundarya.aitriage.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthCheckController {

    @GetMapping("/api/v1/health")
    public Map<String, String> healthCheck() {
        return Map.of(
                "status", "UP",
                "service", "AI Diagnostic Ticket Triage API"
        );
    }
}