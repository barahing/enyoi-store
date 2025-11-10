package com.store.notifications_microservice;

import java.util.Arrays;

import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class NotificationsMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationsMicroserviceApplication.class, args);
	}

	@Bean
    CommandLineRunner debugRabbitListeners(RabbitListenerEndpointRegistry registry) {
        return args -> {
            log.info("📡 Listando RabbitMQ listeners registrados...");
            registry.getListenerContainers().forEach(container -> {
                if (container instanceof AbstractMessageListenerContainer c) {
                    String id = c.getListenerId(); // disponible en clase abstracta
                    String[] queues = c.getQueueNames(); // disponible en clase abstracta
                    log.info(" - Listener ID: {} | Queues: {}", id, Arrays.toString(queues));
                } else {
                    log.info(" - Container: {} (tipo: {})", container, container.getClass().getName());
                }
            });
        };
    }

}
