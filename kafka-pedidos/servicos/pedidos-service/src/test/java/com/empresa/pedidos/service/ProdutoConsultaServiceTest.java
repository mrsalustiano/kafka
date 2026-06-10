package com.empresa.pedidos.service;

import com.empresa.pedidos.cache.ProdutoCacheService;
import com.empresa.pedidos.dto.ProdutoCacheDto;
import com.empresa.pedidos.entity.Produto;
import com.empresa.pedidos.exception.DatabaseException;
import com.empresa.pedidos.exception.ValidationException;
import com.empresa.pedidos.mapper.PedidoMapper;
import com.empresa.pedidos.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoConsultaServiceTest {

    @Mock
    private ProdutoCacheService produtoCacheService;
    @Mock
    private ProdutoRepository produtoRepository;
    @Mock
    private PedidoMapper pedidoMapper;

    @InjectMocks
    private ProdutoConsultaService produtoConsultaService;

    private final ProdutoCacheDto cacheDto = new ProdutoCacheDto(
            1L, "Produto", BigDecimal.TEN, "S", LocalDateTime.now(), null);

    @Test
    void buscarProdutoAtivo_quandoNoRedis_deveRetornar() {
        when(produtoCacheService.buscarPorId(1L)).thenReturn(Optional.of(cacheDto));

        ProdutoCacheDto result = produtoConsultaService.buscarProdutoAtivo(1L);

        assertThat(result).isEqualTo(cacheDto);
    }

    @Test
    void buscarProdutoAtivo_quandoRedisInativo_deveLancarValidationException() {
        ProdutoCacheDto inativo = new ProdutoCacheDto(1L, "Produto", BigDecimal.TEN, "N", LocalDateTime.now(), null);
        when(produtoCacheService.buscarPorId(1L)).thenReturn(Optional.of(inativo));

        assertThatThrownBy(() -> produtoConsultaService.buscarProdutoAtivo(1L))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void buscarProdutoAtivo_quandoNaoNoRedis_deveBuscarNoBancoEAtualizarCache() {
        Produto produto = Produto.builder().codigoProduto(1L).descricao("Produto").valor(BigDecimal.TEN).ativo("S").build();
        when(produtoCacheService.buscarPorId(1L)).thenReturn(Optional.empty());
        when(produtoRepository.findAtivoById(1L)).thenReturn(Optional.of(produto));
        when(pedidoMapper.toCacheDto(produto)).thenReturn(cacheDto);

        ProdutoCacheDto result = produtoConsultaService.buscarProdutoAtivo(1L);

        assertThat(result).isEqualTo(cacheDto);
        verify(produtoCacheService).salvarProduto(cacheDto);
    }

    @Test
    void buscarProdutoAtivo_quandoNaoEncontradoNoBanco_deveLancarValidationException() {
        when(produtoCacheService.buscarPorId(1L)).thenReturn(Optional.empty());
        when(produtoRepository.findAtivoById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoConsultaService.buscarProdutoAtivo(1L))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void buscarProdutoAtivo_quandoErroDb_deveLancarDatabaseException() {
        when(produtoCacheService.buscarPorId(1L)).thenReturn(Optional.empty());
        when(produtoRepository.findAtivoById(1L)).thenThrow(new RuntimeException("erro"));

        assertThatThrownBy(() -> produtoConsultaService.buscarProdutoAtivo(1L))
                .isInstanceOf(DatabaseException.class);
    }
}
