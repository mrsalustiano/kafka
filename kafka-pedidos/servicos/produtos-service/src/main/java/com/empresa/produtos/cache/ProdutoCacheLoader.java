package com.empresa.produtos.cache;

import com.empresa.produtos.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProdutoCacheLoader implements ApplicationRunner {

    private final ProdutoService produtoService;
    private final ProdutoCacheService produtoCacheService;

    @Override
    public void run(ApplicationArguments args) {
        if (!produtoCacheService.isDisponivel()) {
            log.warn("Redis indisponivel na inicializacao. Aplicacao continuara sem cache populado.");
            return;
        }
        log.info("Carregando produtos ativos para o Redis...");
        produtoService.recarregarCacheCompleto();
    }
}
