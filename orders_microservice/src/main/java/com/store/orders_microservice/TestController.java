package com.store.orders_microservice;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/")
public class TestController {
    @GetMapping()
    public String helloWorld() {
        return ("Hello workd");
    }
    
}
