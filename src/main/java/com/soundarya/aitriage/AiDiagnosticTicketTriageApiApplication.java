package com.soundarya.aitriage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.soundarya.aitriage.ai.OpenAiProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(OpenAiProperties.class)
public class AiDiagnosticTicketTriageApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiDiagnosticTicketTriageApiApplication.class, args);
    }
}