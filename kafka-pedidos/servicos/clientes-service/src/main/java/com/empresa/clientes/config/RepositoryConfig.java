package com.empresa.clientes.config;

import com.empresa.clientes.repository.AuditoriaRepository;
import com.empresa.clientes.repository.ClienteRepository;
import com.empresa.clientes.repository.ClienteStatusRepository;
import com.empresa.clientes.repository.MensagemProcessadaRepository;
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
    public ClienteStatusRepository clienteStatusRepository(Jdbi jdbi) {
        return jdbi.onDemand(ClienteStatusRepository.class);
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
