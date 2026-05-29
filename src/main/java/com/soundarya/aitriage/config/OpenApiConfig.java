package com.soundarya.aitriage.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI aiDiagnosticTicketTriageOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Diagnostic Ticket Triage API")
                        .version("1.0.0")
                        .description("""
                                Production-grade Java Spring Boot API for AI-powered IT ticket triage.
                                The system supports ticket creation, AI/rule-based triage, OpenAI fallback handling,
                                audit logs, manual override, and PostgreSQL pgvector-based similar ticket search.
                                """)
                        .contact(new Contact()
                                .name("Soundarya Kookanda")
                                .email("your-email@example.com"))
                        .license(new License()
                                .name("MIT License")));
    }
}