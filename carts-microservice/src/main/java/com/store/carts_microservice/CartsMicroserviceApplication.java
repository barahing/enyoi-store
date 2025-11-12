package com.store.carts_microservice;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication(scanBasePackages = "com.store")
@EnableRabbit
public class CartsMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CartsMicroserviceApplication.class, args);
	}

}
