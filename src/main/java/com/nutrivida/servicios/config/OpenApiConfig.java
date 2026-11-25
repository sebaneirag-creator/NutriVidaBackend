package com.nutrivida.servicios.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nutrividaOpenApi() {
        return new OpenAPI().info(new Info()
                .title("API NutriVida")
                .version("1.0")
                .description("Servicios nutricionales: catálogo, usuarios, pedidos y reportes"));
    }
}
