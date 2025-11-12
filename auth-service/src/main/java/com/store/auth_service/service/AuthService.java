package com.store.auth_service.service;

import com.store.common.security.JwtUtil;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class AuthService {

    private final JwtUtil jwtUtil;

    public AuthService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public Mono<String> authenticate(String username, String password) {
        // 👇 Mock de usuarios válidos
        if (("admin".equals(username) && "password".equals(password))
                || ("ricardo.asfsaf@conexpro.net".equals(username) && "securepassword123".equals(password))) {

            // claims de ejemplo
            Map<String, Object> claims = Map.of(
                    "role", "ADMIN",
                    "scope", "carts:read carts:write"
            );

            String token = jwtUtil.generateToken(username);
            return Mono.just(token);
        }

        return Mono.error(new RuntimeException("Invalid credentials"));
    }
}
