package com.empresa.pedidos.config;

import com.empresa.pedidos.repository.AuditoriaRepository;
import com.empresa.pedidos.repository.ClienteRepository;
import com.empresa.pedidos.repository.MensagemProcessadaRepository;
import com.empresa.pedidos.repository.PedidoRepository;
import com.empresa.pedidos.repository.ProdutoRepository;
import org.jdbi.v3.core.Jdbi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfig {

    @Bean
    public PedidoRepository pedidoRepository(Jdbi jdbi) {
        return jdbi.onDemand(PedidoRepository.class);
    }

    @Bean
    public ClienteRepository clienteRepository(Jdbi jdbi) {
        return jdbi.onDemand(ClienteRepository.class);
    }

    @Bean
    public ProdutoRepository produtoRepository(Jdbi jdbi) {
        return jdbi.onDemand(ProdutoRepository.class);
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
