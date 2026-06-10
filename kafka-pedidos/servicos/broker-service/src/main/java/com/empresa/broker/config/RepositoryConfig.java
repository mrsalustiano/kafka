package com.empresa.broker.config;

import com.empresa.broker.repository.AuditoriaRepository;
import com.empresa.broker.repository.ClienteRepository;
import com.empresa.broker.repository.MensagemProcessadaRepository;
import org.jdbi.v3.core.Jdbi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfig {

    @Bean
    public ClienteRepository clienteRepository(Jdbi jdbi) {
        return jdbi.onDemand(ClienteRepository.class);
    }

    @Bean
    public AuditoriaRepository auditoriaRepository(Jdbi jdbi) {
        return jdbi.onDemand(AuditoriaRepository.class);
    }

    @Bean
    public MensagemProcessadaRepository mensagemProcessadaRepository(Jdbi jdbi) {
        return jdbi.onDemand(MensagemProcessadaRepository.class);
    }
}
