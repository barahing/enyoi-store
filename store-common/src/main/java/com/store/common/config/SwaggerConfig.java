package com.store.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI globalOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Enyoi Store APIs")
                        .description("Documentación unificada para microservicios Enyoi Store")
                        .version("1.0.0"));
    }
}
