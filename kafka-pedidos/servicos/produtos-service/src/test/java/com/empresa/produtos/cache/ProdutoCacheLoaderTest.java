package com.empresa.produtos.cache;

import com.empresa.produtos.service.ProdutoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoCacheLoaderTest {

    @Mock
    private ProdutoService produtoService;

    @Mock
    private ProdutoCacheService produtoCacheService;

    @InjectMocks
    private ProdutoCacheLoader produtoCacheLoader;

    @Test
    void run_quandoRedisDisponivel_deveCarregarCache() throws Exception {
        when(produtoCacheService.isDisponivel()).thenReturn(true);

        produtoCacheLoader.run(new DefaultApplicationArguments());

        verify(produtoService).recarregarCacheCompleto();
    }

    @Test
    void run_quandoRedisIndisponivel_naoDeveCarregarCache() throws Exception {
        when(produtoCacheService.isDisponivel()).thenReturn(false);

        produtoCacheLoader.run(new DefaultApplicationArguments());

        verify(produtoService, never()).recarregarCacheCompleto();
    }
}
