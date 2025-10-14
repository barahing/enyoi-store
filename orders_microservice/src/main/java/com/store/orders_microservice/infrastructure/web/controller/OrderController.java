package com.store.orders_microservice.infrastructure.web.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.store.orders_microservice.application.exception.OrderNotFoundException;
import com.store.orders_microservice.domain.ports.in.IOrderUseCases;
import com.store.orders_microservice.infrastructure.web.dto.OrderRequestDto;
import com.store.orders_microservice.infrastructure.web.dto.OrderResponseDto;
import com.store.orders_microservice.infrastructure.web.dto.OrderItemRequestDto;
import com.store.orders_microservice.infrastructure.web.mapper.OrderItemMapperDto;
import com.store.orders_microservice.infrastructure.web.mapper.OrderMapperDto;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final IOrderUseCases orderUseCases;
    private final OrderMapperDto orderMapper;
    private final OrderItemMapperDto orderItemMapper;

    @PostMapping
    public Mono<OrderResponseDto> createOrder(@Valid @RequestBody OrderRequestDto orderRequestDto){
        return orderUseCases.createOrder(orderMapper.toDomain(orderRequestDto))
            .map(orderMapper::toResponseDto);
    }

    @GetMapping
    public Flux<OrderResponseDto> getAllOrders() {
        return orderUseCases.getAllOrders()
            .map (orderMapper::toResponseDto);
    }

    @GetMapping("/{orderId}")
    public Mono<OrderResponseDto> getOrderById(@PathVariable UUID orderId){
        return orderUseCases.getOrderById(orderId)
            .map(orderMapper::toResponseDto);
            
    }

   @PutMapping("/{orderId}/modify")
    public Mono<ResponseEntity<OrderResponseDto>> modifyOrder(
        @PathVariable UUID orderId,
        @Valid @RequestBody OrderRequestDto orderRequestDto) {

    return orderUseCases.modifyOrder(orderId, orderMapper.toDomain(orderRequestDto))
            .map(orderMapper::toResponseDto)
            .map(ResponseEntity::ok)
            .onErrorResume(OrderNotFoundException.class, e ->
                    Mono.just(ResponseEntity.notFound().build()));
}

    @PutMapping("/{orderId}")
    public Mono<OrderResponseDto> updateOrder(@PathVariable UUID orderId,@Valid @RequestBody OrderRequestDto orderRequestDto){
        return orderUseCases.updateOrder(orderId, orderMapper.toDomain(orderRequestDto))
            .map(orderMapper::toResponseDto);
    }

    
    @PatchMapping("/{orderId}/confirm")
    public Mono<OrderResponseDto> confirmOrder(@PathVariable UUID orderId){
        return orderUseCases.confirmOrder(orderId)
        .map(orderMapper::toResponseDto);
    }

    @PatchMapping("/{orderId}/ship")
    public Mono<OrderResponseDto> shipOrder(@PathVariable UUID orderId){
        return orderUseCases.shipOrder(orderId)
        .map(orderMapper::toResponseDto);        
    }

    @PatchMapping("/{orderId}/deliver")
    public Mono<OrderResponseDto> deliverOrder(@PathVariable UUID orderId){
        return orderUseCases.deliverOrder(orderId)
        .map(orderMapper::toResponseDto);        
    }

    @PatchMapping("/{orderId}/cancel")
    public Mono<OrderResponseDto> cancelOrder(@PathVariable UUID orderId){
        return orderUseCases.cancelOrder(orderId)
        .map(orderMapper::toResponseDto);        
    }

    // 🔹 Eliminar una orden
    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Map<String, String>>> deleteOrder(@PathVariable UUID orderId){
        return orderUseCases.deleteOrder(orderId)
            .thenReturn(ResponseEntity.ok(Map.of("order", "deleted", "id", orderId.toString())));
    }

    @PatchMapping("/{orderId}/items")
    public Mono<OrderResponseDto> addItemToOrder(@PathVariable UUID orderId, @Valid @RequestBody OrderItemRequestDto itemRequestDto){
        return orderUseCases.getOrderById(orderId)
            .flatMap (order -> {
                order.addItem(orderItemMapper.toDomain(itemRequestDto));
                return orderUseCases.updateOrder(orderId, order);
            })
            .map(orderMapper::toResponseDto);

    }

    @DeleteMapping("/{orderId}/items/{productId}")
    public Mono<OrderResponseDto> removeItemFromOrder(@PathVariable UUID orderId, @PathVariable UUID productId) {
        return orderUseCases.getOrderById(orderId)
                .flatMap(order -> {
                    order.removeItem(productId);
                    return orderUseCases.updateOrder(orderId, order);
                })
                .map(orderMapper::toResponseDto);
    }

}
