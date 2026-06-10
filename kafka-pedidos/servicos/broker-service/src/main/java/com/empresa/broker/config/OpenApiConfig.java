package com.empresa.broker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI brokerOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Broker Service API")
                        .description("Microsservico broker Kafka - kafka-pedidos")
                        .version("v1")
                        .contact(new Contact().name("Empresa")));
    }
}
