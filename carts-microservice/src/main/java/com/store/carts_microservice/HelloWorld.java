package com.store.carts_microservice;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/carts")
public class HelloWorld {
    @GetMapping()
    public String helloWorld() {
        return "Hello world";
    }
    
}
