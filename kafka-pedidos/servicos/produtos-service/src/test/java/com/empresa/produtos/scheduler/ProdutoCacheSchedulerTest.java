package com.empresa.produtos.scheduler;

import com.empresa.produtos.config.CacheProperties;
import com.empresa.produtos.service.ProdutoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoCacheSchedulerTest {

    @Mock
    private ProdutoService produtoService;

    @Mock
    private CacheProperties cacheProperties;

    @InjectMocks
    private ProdutoCacheScheduler scheduler;

    @Test
    void atualizarCache_deveRecarregarProdutos() {
        when(cacheProperties.getRefreshMinutes()).thenReturn(30);

        scheduler.atualizarCache();

        verify(produtoService).recarregarCacheCompleto();
    }
}
