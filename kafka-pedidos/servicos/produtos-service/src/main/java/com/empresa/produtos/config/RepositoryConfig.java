package com.empresa.produtos.config;

import com.empresa.produtos.repository.AuditoriaRepository;
import com.empresa.produtos.repository.ProdutoRepository;
import org.jdbi.v3.core.Jdbi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfig {

    @Bean
    public ProdutoRepository produtoRepository(Jdbi jdbi) {
        return jdbi.onDemand(ProdutoRepository.class);
    }

    @Bean
    public AuditoriaRepository auditoriaRepository(Jdbi jdbi) {
        return jdbi.onDemand(AuditoriaRepository.class);
    }
}
