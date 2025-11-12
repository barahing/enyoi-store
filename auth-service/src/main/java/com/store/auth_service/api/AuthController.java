package com.store.auth_service;

import com.store.common.security.JwtUtil;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Map<String, String>> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        // 🔧 Simulación de validación (luego se conecta al microservicio users)
        if ("admin".equals(username) && "password".equals(password)) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", "ADMIN");
            claims.put("scope", "carts:read carts:write");

            String token = jwtUtil.generateToken(username);
            return Mono.just(Map.of("token", token));
        }
        return Mono.error(new RuntimeException("Invalid credentials"));
    }
}
