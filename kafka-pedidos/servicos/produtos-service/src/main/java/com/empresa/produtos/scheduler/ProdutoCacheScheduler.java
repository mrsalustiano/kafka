package com.empresa.produtos.scheduler;

import com.empresa.produtos.config.CacheProperties;
import com.empresa.produtos.service.ProdutoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProdutoCacheScheduler {

    private final ProdutoService produtoService;
    private final CacheProperties cacheProperties;

    @Scheduled(fixedDelayString = "#{${app.cache.produtos.refresh-minutes:30} * 60 * 1000}")
    public void atualizarCache() {
        log.info("Scheduler: atualizando cache de produtos (intervalo: {} minutos)",
                cacheProperties.getRefreshMinutes());
        produtoService.recarregarCacheCompleto();
    }
}
