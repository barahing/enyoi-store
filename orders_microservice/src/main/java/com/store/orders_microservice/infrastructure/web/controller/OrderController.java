package com.store.orders_microservice.infrastructure.web.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.store.orders_microservice.domain.ports.in.IOrderServicePort;
import com.store.orders_microservice.infrastructure.web.dto.OrderItemRequestDto;
import com.store.orders_microservice.infrastructure.web.dto.OrderResponseDto;
import com.store.orders_microservice.infrastructure.web.mapper.IOrderMapperDto;
import com.store.orders_microservice.infrastructure.web.mapper.IOrderItemMapperDto;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {
    
    private final IOrderServicePort orderServicePort;
    private final IOrderMapperDto orderMapper;
    private final IOrderItemMapperDto orderItemMapper;

    @GetMapping("/{orderId}")
    public Mono<OrderResponseDto> getOrderById(@PathVariable UUID orderId) {
        return orderServicePort.getOrderById(orderId)
                .map(orderMapper::toResponseDto);
    }

    @GetMapping
    public Flux<OrderResponseDto> getAllOrders() {
        return orderServicePort.getAllOrders()
                .map(orderMapper::toResponseDto);
    }
    
    @PutMapping("/{orderId}/confirm")
    public Mono<OrderResponseDto> confirmOrder(@PathVariable UUID orderId) {
        return orderServicePort.confirmOrder(orderId)
                .map(orderMapper::toResponseDto);
    }

    @PutMapping("/{orderId}/ship")
    public Mono<OrderResponseDto> shipOrder(@PathVariable UUID orderId) {
        return orderServicePort.shipOrder(orderId)
                .map(orderMapper::toResponseDto);
    }

    @PutMapping("/{orderId}/deliver")
    public Mono<OrderResponseDto> deliverOrder(@PathVariable UUID orderId) {
        return orderServicePort.deliverOrder(orderId)
                .map(orderMapper::toResponseDto);
    }

    @PutMapping("/{orderId}/cancel")
    public Mono<OrderResponseDto> cancelOrder(@PathVariable UUID orderId) {
        return orderServicePort.cancelOrder(orderId)
                .map(orderMapper::toResponseDto);
    }
    
    @PostMapping("/{orderId}/items")
    public Mono<OrderResponseDto> addItemToOrder(
            @PathVariable UUID orderId,
            @RequestBody @Validated OrderItemRequestDto itemDto) {
        
        return orderServicePort.addProductToOrder(
                        orderId, 
                        orderItemMapper.toDomain(itemDto)
                    )
                    .map(orderMapper::toResponseDto);
    }

    @PutMapping("/{orderId}/items/{productId}")
    public Mono<OrderResponseDto> updateItemQuantity(
            @PathVariable UUID orderId,
            @PathVariable UUID productId,
            @RequestBody @Validated OrderItemRequestDto itemDto) {
        
        return orderServicePort.updateItemQuantity(
                        orderId, 
                        productId, 
                        itemDto.getQuantity()
                    )
                    .map(orderMapper::toResponseDto);
    }

    @DeleteMapping("/{orderId}/items/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> removeItemFromOrder(
            @PathVariable UUID orderId,
            @PathVariable UUID productId) {
        return orderServicePort.removeProductFromOrder(orderId, productId).then();
    }
    
    /*@DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> hardDeleteOrder(@PathVariable UUID orderId) {
        return orderServicePort.deleteOrder(orderId);
    }*/
}