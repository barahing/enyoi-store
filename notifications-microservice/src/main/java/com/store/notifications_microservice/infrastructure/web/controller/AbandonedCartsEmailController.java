package com.store.notifications_microservice.infrastructure.web.controller;

import com.store.notifications_microservice.domain.ports.out.IBrevoSenderPorts;
import com.store.notifications_microservice.infrastructure.web.dto.EmailSendRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.File;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class AbandonedCartsEmailController {

    private final IBrevoSenderPorts brevoSenderPorts;

    @PostMapping("/abandoned-carts")
    public Mono<Void> sendAbandonedCartsReport(@RequestBody EmailSendRequest request) {
        log.info("📩 [RECEIVED] Abandoned carts report email request for {}", request.getTo());

        File attachment = request.getAttachmentPath() != null ? new File(request.getAttachmentPath()) : null;

        return brevoSenderPorts.sendTransactionalEmail(
                request.getTo(),
                request.getSubject(),
                request.getHtml(),
                attachment
        );
    }
}
