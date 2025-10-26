package com.store.orders_microservice.infrastructure.persistence.adapter;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.store.orders_microservice.domain.model.Order;
import com.store.orders_microservice.domain.ports.out.IOrderRepositoryPort;
import com.store.orders_microservice.infrastructure.persistence.mapper.IOrderItemEntityMapper;
import com.store.orders_microservice.infrastructure.persistence.mapper.IOrderEntityMapper;
import com.store.orders_microservice.infrastructure.persistence.repository.IOrderItemR2dbcRepository;
import com.store.orders_microservice.infrastructure.persistence.repository.IOrderR2dbcRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements IOrderRepositoryPort{
    private final IOrderEntityMapper mapper;
    private final IOrderItemEntityMapper itemMapper;
    private final IOrderR2dbcRepository orderRepository;
    private final IOrderItemR2dbcRepository itemRepository;

    
    @Override
    public Mono<Order> save(Order order) {
        
        Mono<Void> itemCleanup;
        if (order.getOrderId() != null) {
            itemCleanup = itemRepository.deleteAllByOrderId(order.getOrderId());
        } else {
            itemCleanup = Mono.empty();
        }
        
        return orderRepository.save(mapper.toEntity(order))
            .flatMap(savedEntity -> {
                UUID orderId = savedEntity.getId();
                
                return itemCleanup
                    .thenMany(Flux.fromIterable(order.getItems()))
                    .map(item -> {
                        var entity = itemMapper.toEntity(item);
                        entity.setOrderId(orderId);
                        return entity;
                    })
                    .collectList()
                    .flatMapMany(itemRepository::saveAll)
                    .collectList()
                    .thenReturn(savedEntity);
            })
            .flatMap(savedEntity -> 
                itemRepository.findByOrderId(savedEntity.getId())
                    .map(itemMapper::toDomain)
                    .collectList()
                    .map(savedItems -> {
                        Order domainOrder = mapper.toDomain(savedEntity);
                        domainOrder.setItems(savedItems); 
                        return domainOrder;
                    })
            );
    }

    @Override
    public Mono<Order> findById(UUID id) {
        return orderRepository.findById(id)
            .flatMap(orderEntity ->
                itemRepository.findByOrderId(orderEntity.getId())
                    .map(itemMapper::toDomain)
                    .collectList()
                    .map(items -> {
                        Order order = mapper.toDomain(orderEntity);
                        order.setItems(items);
                        return order;
                    })
            );
    }

    @Override
    public Flux<Order> findAll() {
        return orderRepository.findAll()
            .flatMap(orderEntity ->
                itemRepository.findByOrderId(orderEntity.getId())
                    .map(itemMapper::toDomain)
                    .collectList()
                    .map(items -> {
                        Order order = mapper.toDomain(orderEntity);
                        order.setItems(items);
                        return order;
                    })
            );
    }

 
    //@Override 
    /*public Mono<Void> deleteById(UUID id) {
        return orderRepository.deleteById(id);
    }*/
}