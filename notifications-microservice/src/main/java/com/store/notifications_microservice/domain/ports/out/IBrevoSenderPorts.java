package com.store.notifications_microservice.domain.ports.out;

import java.io.File;

import reactor.core.publisher.Mono;

public interface IBrevoSenderPorts {
    
    Mono<Void> sendTransactionalEmail(String recipientEmail, String subject, String bodyHtml);

    default Mono<Void> sendTransactionalEmail(String recipientEmail, String subject, String bodyHtml, File attachment) {
        return sendTransactionalEmail(recipientEmail, subject, bodyHtml);
    }
}