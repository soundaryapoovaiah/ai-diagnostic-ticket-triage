package com.soundarya.aitriage.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soundarya.aitriage.ticket.Severity;
import com.soundarya.aitriage.ticket.Ticket;
import com.soundarya.aitriage.ticket.TicketCategory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class OpenAiTriageClient {

    private final WebClient webClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    public OpenAiTriageClient(OpenAiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .build();
    }

    public AiTriageResult triage(Ticket ticket) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY is not configured");
        }

        String prompt = buildPrompt(ticket);
        String requestBody = buildRequestBody(prompt);

        String response = webClient.post()
                .uri("/responses")
                .header("Authorization", "Bearer " + properties.apiKey())
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return parseResponse(response);
    }

    private String buildPrompt(Ticket ticket) {
        return """
                You are an enterprise IT incident triage assistant.

                Analyze this support ticket and return ONLY valid JSON.

                Allowed severity values:
                LOW, MEDIUM, HIGH, CRITICAL

                Allowed category values:
                DATABASE_CONNECTIVITY, API_FAILURE, DEPLOYMENT_ISSUE, AUTHENTICATION, NETWORK, PERFORMANCE, UNKNOWN

                Ticket:
                Title: %s
                Description: %s
                Environment: %s
                Source: %s

                Return JSON with these fields:
                category, severity, assignedTeam, rootCauseHypothesis, suggestedFix, confidenceScore
                """.formatted(
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getEnvironment(),
                ticket.getSource()
        );
    }

    private String buildRequestBody(String prompt) {
        try {
            return objectMapper.writeValueAsString(
                    Map.of(
                            "model", properties.model(),
                            "input", prompt,
                            "text", Map.of(
                                    "format", Map.of(
                                            "type", "json_object"
                                    )
                            )
                    )
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to build OpenAI request body", ex);
        }
    }

    private AiTriageResult parseResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);

            String outputText = root.path("output")
                    .get(0)
                    .path("content")
                    .get(0)
                    .path("text")
                    .asText();

            JsonNode triageJson = objectMapper.readTree(outputText);

            TicketCategory category = TicketCategory.valueOf(triageJson.path("category").asText());
            Severity severity = Severity.valueOf(triageJson.path("severity").asText());
            String assignedTeam = triageJson.path("assignedTeam").asText();
            String rootCause = triageJson.path("rootCauseHypothesis").asText();
            String suggestedFix = triageJson.path("suggestedFix").asText();
            double confidenceScore = triageJson.path("confidenceScore").asDouble();

            return new AiTriageResult(
                    category,
                    severity,
                    assignedTeam,
                    rootCause,
                    suggestedFix,
                    confidenceScore,
                    outputText
            );

        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse OpenAI triage response", ex);
        }
    }
}