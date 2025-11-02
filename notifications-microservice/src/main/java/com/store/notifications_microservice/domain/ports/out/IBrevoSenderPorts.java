package com.store.notifications_microservice.domain.ports.out;

import reactor.core.publisher.Mono;

public interface IBrevoSenderPorts {
    
    Mono<Void> sendTransactionalEmail(String recipientEmail, String subject, String bodyHtml);
}