package com.store.carts_microservice.infrastructure.config;

import com.store.common.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import reactor.core.publisher.Mono;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(auth -> auth
                        .pathMatchers(
                                "/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs/**", "/api-docs/**",
                                "/actuator/**", "/api/public/**"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtDecoder(reactiveJwtDecoder()))
                )
                .build();
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return token -> {
            try {
                Jwt decoded = jwtUtil.decode(token); 
                return Mono.just(decoded);
            } catch (Exception e) {
                return Mono.error(e);
            }
        };
    }
}
