// file: store-common/src/main/java/com/store/common/security/SecurityConfigCommon.java
package com.store.common.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration(proxyBeanMethods = false)
public class SecurityConfigCommon {

    @Bean(name = "securityWebFilterChainCommon")
    @ConditionalOnMissingBean(SecurityWebFilterChain.class)
    public SecurityWebFilterChain securityWebFilterChainCommon(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(auth -> auth
                .pathMatchers(
                    "/swagger-ui.html", "/swagger-ui/**",
                    "/v3/api-docs/**", "/api-docs/**",
                    "/actuator/**", "/api/public/**"
                ).permitAll()
                .anyExchange().permitAll()
            )
            .build();
    }
}
