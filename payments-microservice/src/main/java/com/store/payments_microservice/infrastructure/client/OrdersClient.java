package com.store.payments_microservice.infrastructure.client;

import com.store.payments_microservice.infrastructure.dto.OrderDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class OrdersClient {

    private final WebClient webClient;

    public OrdersClient(@Value("${app.services.orders.url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<OrderDTO> getOrderById(String orderId) {
        return webClient.get()
                .uri("/{id}", orderId)
                .retrieve()
                .bodyToMono(OrderDTO.class);
    }
}
