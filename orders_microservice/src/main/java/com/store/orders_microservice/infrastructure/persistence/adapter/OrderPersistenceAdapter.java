package com.store.orders_microservice.infrastructure.persistence.adapter;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.store.orders_microservice.domain.model.Order;
import com.store.orders_microservice.domain.ports.out.IOrderPersistencePort;
import com.store.orders_microservice.infrastructure.persistence.mapper.IOrderItemMapperEntity;
import com.store.orders_microservice.infrastructure.persistence.mapper.IOrderMapperEntity;
import com.store.orders_microservice.infrastructure.persistence.repository.IOrderItemR2dbcRepository;
import com.store.orders_microservice.infrastructure.persistence.repository.IOrderR2dbcRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements IOrderPersistencePort{
    private final IOrderMapperEntity mapper;
    private final IOrderItemMapperEntity itemMapper;
    private final IOrderR2dbcRepository orderRepository;
    private final IOrderItemR2dbcRepository itemRepository;

   
    @Override
    public Mono<Order> saveOrder(Order order) {
        return orderRepository.save(mapper.toEntity(order))
            .flatMap(savedEntity -> {
                UUID orderId = savedEntity.getId();

                return Flux.fromIterable(order.getItems())
                    .map(item -> {
                        var entity = itemMapper.toEntity(item);
                        entity.setOrderId(orderId);
                        return entity;
                    })
                    .collectList()
                    .flatMapMany(itemRepository::saveAll)
                    .then(itemRepository.findByOrderId(orderId)      // 🔹 recupera los ítems recién guardados
                        .map(itemMapper::toDomain)
                        .collectList()
                        .map(savedItems -> {
                            Order domainOrder = mapper.toDomain(savedEntity);
                            domainOrder.setItems(savedItems);        // 🔹 asocia los ítems
                            domainOrder.recalculateTotal();          // 🔹 recalcula total
                            return domainOrder;
                        })
                    );
            });
    }


    @Override
    public Mono<Order> findOrderById(UUID id) {
        return orderRepository.findById(id)
            .flatMap(orderEntity ->
                itemRepository.findByOrderId(orderEntity.getId())
                    .map(itemMapper::toDomain)
                    .collectList()
                    .map(items -> {
                        Order order = mapper.toDomain(orderEntity);
                        order.setItems(items);
                        order.recalculateTotal();
                        return order;
                    })
            );
    }

    @Override
    public Flux<Order> findAllOrders() {
        return orderRepository.findAll()
            .flatMap(orderEntity ->
                itemRepository.findByOrderId(orderEntity.getId())
                    .map(itemMapper::toDomain)
                    .collectList()
                    .map(items -> {
                        Order order = mapper.toDomain(orderEntity);
                        order.setItems(items);
                        order.recalculateTotal();
                        return order;
                    })
            );
    }


    @Override
    public Mono<Order> updateOrder(Order order) {
        return orderRepository.save(mapper.toEntity(order))
            .flatMap(savedEntity -> {
                UUID orderId = savedEntity.getId();
                return itemRepository.findByOrderId(orderId)
                    .flatMap(existing -> itemRepository.deleteById(existing.getId()))
                    .thenMany(Flux.fromIterable(order.getItems()))
                    .map(itemMapper::toEntity)
                    .doOnNext(item -> item.setOrderId(orderId))
                    .collectList()
                    .flatMapMany(itemRepository::saveAll)
                    .then(Mono.just(savedEntity));
            })
            .map(mapper::toDomain);
    }


    @Override
    public Mono<Void> deleteOrder(UUID id) {
        return orderRepository.deleteById(id);
    }

}
