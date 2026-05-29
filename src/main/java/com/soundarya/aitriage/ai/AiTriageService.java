package com.soundarya.aitriage.ai;

import com.soundarya.aitriage.ticket.Severity;
import com.soundarya.aitriage.ticket.Ticket;
import com.soundarya.aitriage.ticket.TicketCategory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AiTriageService {

    private final TicketAiTriageRepository ticketAiTriageRepository;
    //private final TicketAiTriageRepository ticketAiTriageRepository;
    private final OpenAiTriageClient openAiTriageClient;

    public AiTriageService(TicketAiTriageRepository ticketAiTriageRepository, OpenAiTriageClient openAiTriageClient) {
        this.ticketAiTriageRepository = ticketAiTriageRepository;
        this.openAiTriageClient = openAiTriageClient;
    }

    public AiTriageResult triage(Ticket ticket) {
        AiTriageResult result;
        String modelName;
        String promptVersion;

        try {
            System.out.println("Calling OpenAI triage...");

            result = openAiTriageClient.triage(ticket);

            System.out.println("OpenAI triage success");

            modelName = "OPENAI_GPT_4O_MINI";
            promptVersion = "openai-v1";

        } catch (Exception ex) {
            System.out.println("OpenAI triage failed: " + ex.getMessage());

            result = runRuleBasedTriage(ticket);

            modelName = "RULE_BASED_TRIAGE_V1";
            promptVersion = "fallback-v1";
        }

        TicketAiTriage triageRecord = new TicketAiTriage();
        triageRecord.setTicketId(ticket.getId());
        triageRecord.setModelName(modelName);
        triageRecord.setPromptVersion(promptVersion);
        triageRecord.setConfidenceScore(BigDecimal.valueOf(result.confidenceScore()));
        triageRecord.setRootCauseHypothesis(result.rootCauseHypothesis());
        triageRecord.setSuggestedFix(result.suggestedFix());
        triageRecord.setRawAiResponse(result.rawAiResponse());

        ticketAiTriageRepository.save(triageRecord);

        return result;
    }

    private TicketCategory determineCategory(String text) {
        if (text.contains("database") || text.contains("db") || text.contains("hikari") || text.contains("connection pool")) {
            return TicketCategory.DATABASE_CONNECTIVITY;
        }
        if (text.contains("timeout") || text.contains("504") || text.contains("api")) {
            return TicketCategory.API_FAILURE;
        }
        if (text.contains("deployment") || text.contains("release") || text.contains("rollback")) {
            return TicketCategory.DEPLOYMENT_ISSUE;
        }
        if (text.contains("login") || text.contains("auth") || text.contains("token")) {
            return TicketCategory.AUTHENTICATION;
        }
        if (text.contains("latency") || text.contains("slow") || text.contains("performance")) {
            return TicketCategory.PERFORMANCE;
        }
        return TicketCategory.UNKNOWN;
    }

    private Severity determineSeverity(String text, String environment) {
        boolean isProd = environment != null && environment.equalsIgnoreCase("PROD");

        if (isProd && (text.contains("down") || text.contains("data loss") || text.contains("payment") || text.contains("504"))) {
            return Severity.CRITICAL;
        }
        if (isProd && (text.contains("timeout") || text.contains("error") || text.contains("failure"))) {
            return Severity.HIGH;
        }
        if (text.contains("slow") || text.contains("latency")) {
            return Severity.MEDIUM;
        }
        return Severity.LOW;
    }

    private String determineAssignedTeam(TicketCategory category) {
        return switch (category) {
            case DATABASE_CONNECTIVITY -> "PLATFORM_DB_TEAM";
            case API_FAILURE -> "BACKEND_API_TEAM";
            case DEPLOYMENT_ISSUE -> "DEVOPS_RELEASE_TEAM";
            case AUTHENTICATION -> "IDENTITY_ACCESS_TEAM";
            case NETWORK -> "NETWORK_OPERATIONS_TEAM";
            case PERFORMANCE -> "SITE_RELIABILITY_TEAM";
            case UNKNOWN -> "L1_SUPPORT_TEAM";
        };
    }

    private String determineRootCause(TicketCategory category) {
        return switch (category) {
            case DATABASE_CONNECTIVITY -> "Possible database connection pool exhaustion or database connectivity issue.";
            case API_FAILURE -> "Possible backend API failure, timeout, or upstream service dependency issue.";
            case DEPLOYMENT_ISSUE -> "Possible regression introduced during a recent deployment.";
            case AUTHENTICATION -> "Possible authentication token, login, or identity provider issue.";
            case NETWORK -> "Possible network routing, DNS, or connectivity issue.";
            case PERFORMANCE -> "Possible application latency, slow query, or resource saturation issue.";
            case UNKNOWN -> "Insufficient information to identify a precise root cause.";
        };
    }

    private String determineSuggestedFix(TicketCategory category) {
        return switch (category) {
            case DATABASE_CONNECTIVITY -> "Check Hikari pool size, database CPU, slow queries, and recent configuration changes.";
            case API_FAILURE -> "Check API logs, dependency health, gateway timeout metrics, and recent deployments.";
            case DEPLOYMENT_ISSUE -> "Compare deployment versions, inspect release logs, and consider rollback if production impact is high.";
            case AUTHENTICATION -> "Validate token expiration, identity provider status, and authentication service logs.";
            case NETWORK -> "Check DNS resolution, firewall rules, routing tables, and network latency.";
            case PERFORMANCE -> "Review application metrics, slow endpoints, database indexes, and JVM resource usage.";
            case UNKNOWN -> "Collect more logs, service name, error code, environment, and reproduction steps.";
        };
    }
    private AiTriageResult runRuleBasedTriage(Ticket ticket) {
        String text = (ticket.getTitle() + " " + ticket.getDescription()).toLowerCase();

        TicketCategory category = determineCategory(text);
        Severity severity = determineSeverity(text, ticket.getEnvironment());
        String assignedTeam = determineAssignedTeam(category);
        String rootCause = determineRootCause(category);
        String suggestedFix = determineSuggestedFix(category);
        double confidenceScore = 0.86;

        String rawResponse = """
            {
              "category": "%s",
              "severity": "%s",
              "assignedTeam": "%s",
              "rootCauseHypothesis": "%s",
              "suggestedFix": "%s",
              "confidenceScore": %.2f
            }
            """.formatted(
                category,
                severity,
                assignedTeam,
                rootCause,
                suggestedFix,
                confidenceScore
        );

        return new AiTriageResult(
                category,
                severity,
                assignedTeam,
                rootCause,
                suggestedFix,
                confidenceScore,
                rawResponse
        );
    }
}