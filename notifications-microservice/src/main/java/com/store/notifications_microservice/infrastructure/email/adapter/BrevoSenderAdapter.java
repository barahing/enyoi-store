package com.store.notifications_microservice.infrastructure.email.adapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import com.store.notifications_microservice.domain.ports.out.IBrevoSenderPorts;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class BrevoSenderAdapter implements IBrevoSenderPorts {

    private final WebClient webClient;
    private final String senderEmail;
    private final String senderName;

    public BrevoSenderAdapter(
        WebClient.Builder webClientBuilder,
        @Value("${app.brevo.api-key}") String apiKey,
        @Value("${app.brevo.sender-email}") String senderEmail,
        @Value("${app.brevo.sender-name}") String senderName) {
            
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        
        this.webClient = webClientBuilder
            .baseUrl("https://api.brevo.com/v3/smtp/email")
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("api-key", apiKey)
            .build();
    }

    @Override
    public Mono<Void> sendTransactionalEmail(String recipientEmail, String subject, String bodyHtml) {
        
        String requestBody = String.format("""
        {
          "sender": {"name": "%s", "email": "%s"},
          "to": [{"email": "%s"}],
          "subject": "%s",
          "htmlContent": "%s"
        }
        """, senderName, senderEmail, recipientEmail, subject, bodyHtml.replace("\n", ""));

        return webClient.post()
            .bodyValue(requestBody)
            .retrieve()
            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), 
                      response -> response.bodyToMono(String.class)
                                          .flatMap(error -> {
                                              log.error("Error sending email via Brevo. Recipient: {}. Response: {}", recipientEmail, error);
                                              return Mono.error(new RuntimeException("Brevo API Error: " + error));
                                          }))
            .bodyToMono(Void.class)
            .doOnSuccess(v -> log.info("Email sent successfully to: {}", recipientEmail))
            .doOnError(e -> log.error("Failed to send email to {}: {}", recipientEmail, e.getMessage()));
    }
}