package com.empresa.produtos.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI produtosOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Produtos Service API")
                        .description("API de gerenciamento de produtos - kafka-pedidos")
                        .version("v1")
                        .contact(new Contact().name("Empresa")));
    }
}
