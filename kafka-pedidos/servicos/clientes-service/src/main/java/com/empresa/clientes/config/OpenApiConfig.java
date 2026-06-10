package com.empresa.clientes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI clientesOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Clientes Service API")
                        .description("API de gerenciamento de clientes - kafka-pedidos")
                        .version("v1")
                        .contact(new Contact().name("Empresa")));
    }
}
