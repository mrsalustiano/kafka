package com.empresa.produtos.service;

import com.empresa.produtos.audit.AuditoriaService;
import com.empresa.produtos.audit.OperacaoAuditoria;
import com.empresa.produtos.cache.ProdutoCacheService;
import com.empresa.produtos.dto.PageResponse;
import com.empresa.produtos.dto.ProdutoRequest;
import com.empresa.produtos.dto.ProdutoResponse;
import com.empresa.produtos.entity.Produto;
import com.empresa.produtos.exception.DatabaseException;
import com.empresa.produtos.exception.NotFoundException;
import com.empresa.produtos.exception.ValidationException;
import com.empresa.produtos.mapper.ProdutoMapper;
import com.empresa.produtos.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private ProdutoMapper produtoMapper;

    @Mock
    private ProdutoCacheService produtoCacheService;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private ProdutoService produtoService;

    private Produto produto;
    private ProdutoResponse produtoResponse;
    private ProdutoRequest produtoRequest;

    @BeforeEach
    void setUp() {
        produto = Produto.builder()
                .codigoProduto(1L)
                .descricao("Produto A")
                .valor(new BigDecimal("10.50"))
                .ativo("S")
                .dataCriacao(LocalDateTime.now())
                .build();

        produtoResponse = new ProdutoResponse(1L, "Produto A", new BigDecimal("10.50"), "S",
                LocalDateTime.now(), null);

        produtoRequest = new ProdutoRequest("Produto A", new BigDecimal("10.50"));
    }

    @Test
    void criar_quandoNaoEncontradoAposInsert_deveLancarNotFoundException() {
        when(produtoMapper.toEntity(produtoRequest)).thenReturn(produto);
        when(produtoRepository.insert(produto)).thenReturn(1L);
        when(produtoRepository.findAtivoById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.criar(produtoRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void criar_deveRetornarProdutoCriado() {
        when(produtoMapper.toEntity(produtoRequest)).thenReturn(produto);
        when(produtoRepository.insert(produto)).thenReturn(1L);
        when(produtoRepository.findAtivoById(1L)).thenReturn(Optional.of(produto));
        when(produtoMapper.toResponse(produto)).thenReturn(produtoResponse);
        when(produtoRepository.findAllAtivos()).thenReturn(List.of(produto));
        when(produtoMapper.toResponseList(List.of(produto))).thenReturn(List.of(produtoResponse));
        doNothing().when(produtoCacheService).salvarProduto(produtoResponse);
        doNothing().when(produtoCacheService).salvarTodos(List.of(produtoResponse));

        ProdutoResponse result = produtoService.criar(produtoRequest);

        assertThat(result).isEqualTo(produtoResponse);
        verify(auditoriaService).registrar(OperacaoAuditoria.CREATE, 1L, "Produto criado: Produto A");
    }

    @Test
    void criar_quandoFalhaPersistencia_deveLancarDatabaseException() {
        when(produtoMapper.toEntity(produtoRequest)).thenReturn(produto);
        when(produtoRepository.insert(produto)).thenThrow(new RuntimeException("erro"));

        assertThatThrownBy(() -> produtoService.criar(produtoRequest))
                .isInstanceOf(DatabaseException.class);
    }

    @Test
    void buscarPorId_quandoEncontradoNoCache_deveRetornarDoCache() {
        when(produtoCacheService.buscarPorId(1L)).thenReturn(Optional.of(produtoResponse));

        ProdutoResponse result = produtoService.buscarPorId(1L);

        assertThat(result).isEqualTo(produtoResponse);
        verify(produtoRepository, never()).findAtivoById(anyLong());
    }

    @Test
    void buscarPorId_quandoNaoEncontradoNoCache_deveBuscarNoBanco() {
        when(produtoCacheService.buscarPorId(1L)).thenReturn(Optional.empty());
        when(produtoRepository.findAtivoById(1L)).thenReturn(Optional.of(produto));
        when(produtoMapper.toResponse(produto)).thenReturn(produtoResponse);

        ProdutoResponse result = produtoService.buscarPorId(1L);

        assertThat(result).isEqualTo(produtoResponse);
        verify(produtoCacheService).salvarProduto(produtoResponse);
    }

    @Test
    void buscarPorId_quandoNaoExiste_deveLancarNotFoundException() {
        when(produtoCacheService.buscarPorId(1L)).thenReturn(Optional.empty());
        when(produtoRepository.findAtivoById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.buscarPorId(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void buscarPorId_quandoErroBanco_deveLancarDatabaseException() {
        when(produtoCacheService.buscarPorId(1L)).thenReturn(Optional.empty());
        when(produtoRepository.findAtivoById(1L)).thenThrow(new RuntimeException("erro"));

        assertThatThrownBy(() -> produtoService.buscarPorId(1L))
                .isInstanceOf(DatabaseException.class);
    }

    @Test
    void listar_deveRetornarPagina() {
        when(produtoRepository.findAllAtivosPaginado("codigo_produto", "ASC", 10, 0))
                .thenReturn(List.of(produto));
        when(produtoRepository.countAtivos()).thenReturn(1L);
        when(produtoMapper.toResponseList(List.of(produto))).thenReturn(List.of(produtoResponse));

        PageResponse<ProdutoResponse> result = produtoService.listar(0, 10, null);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void listar_comSortInvalido_deveLancarValidationException() {
        assertThatThrownBy(() -> produtoService.listar(0, 10, "campo_invalido,asc"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void listar_quandoErroBanco_deveLancarDatabaseException() {
        when(produtoRepository.findAllAtivosPaginado(anyString(), anyString(), anyInt(), anyLong()))
                .thenThrow(new RuntimeException("erro"));

        assertThatThrownBy(() -> produtoService.listar(0, 10, null))
                .isInstanceOf(DatabaseException.class);
    }

    @Test
    void atualizar_deveRetornarProdutoAtualizado() {
        when(produtoRepository.findAtivoById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.update(produto)).thenReturn(1);
        when(produtoMapper.toResponse(produto)).thenReturn(produtoResponse);
        when(produtoRepository.findAllAtivos()).thenReturn(List.of(produto));
        when(produtoMapper.toResponseList(List.of(produto))).thenReturn(List.of(produtoResponse));

        ProdutoResponse result = produtoService.atualizar(1L, produtoRequest);

        assertThat(result).isEqualTo(produtoResponse);
        verify(auditoriaService).registrar(OperacaoAuditoria.UPDATE, 1L, "Produto atualizado: Produto A");
    }

    @Test
    void atualizar_quandoNaoExiste_deveLancarNotFoundException() {
        when(produtoRepository.findAtivoById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.atualizar(1L, produtoRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void atualizar_quandoNenhumaLinhaAtualizada_deveLancarNotFoundException() {
        when(produtoRepository.findAtivoById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.update(produto)).thenReturn(0);

        assertThatThrownBy(() -> produtoService.atualizar(1L, produtoRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void atualizar_quandoNaoEncontradoAposUpdate_deveLancarNotFoundException() {
        when(produtoRepository.findAtivoById(1L))
                .thenReturn(Optional.of(produto))
                .thenReturn(Optional.empty());
        when(produtoRepository.update(produto)).thenReturn(1);

        assertThatThrownBy(() -> produtoService.atualizar(1L, produtoRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void excluirLogicamente_deveDesativarProduto() {
        when(produtoRepository.findAtivoById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.desativar(1L)).thenReturn(1);
        when(produtoRepository.findAllAtivos()).thenReturn(List.of());
        when(produtoMapper.toResponseList(List.of())).thenReturn(List.of());

        produtoService.excluirLogicamente(1L);

        verify(produtoCacheService).removerProduto(1L);
        verify(auditoriaService).registrar(OperacaoAuditoria.DELETE, 1L, "Exclusao logica do produto");
    }

    @Test
    void excluirLogicamente_quandoNaoExiste_deveLancarNotFoundException() {
        when(produtoRepository.findAtivoById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> produtoService.excluirLogicamente(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void excluirLogicamente_quandoNenhumaLinhaDesativada_deveLancarNotFoundException() {
        when(produtoRepository.findAtivoById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.desativar(1L)).thenReturn(0);

        assertThatThrownBy(() -> produtoService.excluirLogicamente(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void excluirLogicamente_quandoErroBanco_deveLancarDatabaseException() {
        when(produtoRepository.findAtivoById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.desativar(1L)).thenThrow(new RuntimeException("erro"));

        assertThatThrownBy(() -> produtoService.excluirLogicamente(1L))
                .isInstanceOf(DatabaseException.class);
    }

    @Test
    void atualizar_quandoErroBanco_deveLancarDatabaseException() {
        when(produtoRepository.findAtivoById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.update(produto)).thenThrow(new RuntimeException("erro"));

        assertThatThrownBy(() -> produtoService.atualizar(1L, produtoRequest))
                .isInstanceOf(DatabaseException.class);
    }

    @Test
    void recarregarCacheCompleto_quandoSucesso_deveSalvarNoRedis() {
        when(produtoRepository.findAllAtivos()).thenReturn(List.of(produto));
        when(produtoMapper.toResponseList(List.of(produto))).thenReturn(List.of(produtoResponse));

        produtoService.recarregarCacheCompleto();

        verify(produtoCacheService).salvarTodos(List.of(produtoResponse));
    }

    @Test
    void recarregarCacheCompleto_quandoFalha_naoDevePropagarExcecao() {
        when(produtoRepository.findAllAtivos()).thenThrow(new RuntimeException("erro"));

        produtoService.recarregarCacheCompleto();

        verify(produtoCacheService, never()).salvarTodos(any());
    }
}
