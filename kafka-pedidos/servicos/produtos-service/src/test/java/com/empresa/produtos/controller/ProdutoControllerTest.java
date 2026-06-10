package com.empresa.produtos.controller;

import com.empresa.produtos.dto.PageResponse;
import com.empresa.produtos.dto.ProdutoRequest;
import com.empresa.produtos.dto.ProdutoResponse;
import com.empresa.produtos.exception.NotFoundException;
import com.empresa.produtos.service.ProdutoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoControllerTest {

    @Mock
    private ProdutoService produtoService;

    @InjectMocks
    private ProdutoController produtoController;

    private final ProdutoRequest request = new ProdutoRequest("Produto", new BigDecimal("9.99"));
    private final ProdutoResponse response = new ProdutoResponse(1L, "Produto", new BigDecimal("9.99"), "S",
            LocalDateTime.now(), null);

    @Test
    void criar_deveRetornar201() {
        when(produtoService.criar(request)).thenReturn(response);

        ResponseEntity<ProdutoResponse> result = produtoController.criar(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void buscarPorId_deveRetornar200() {
        when(produtoService.buscarPorId(1L)).thenReturn(response);

        ResponseEntity<ProdutoResponse> result = produtoController.buscarPorId(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void buscarPorId_quandoNaoEncontrado_devePropagarExcecao() {
        when(produtoService.buscarPorId(1L)).thenThrow(new NotFoundException("nao encontrado"));

        assertThatThrownBy(() -> produtoController.buscarPorId(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void listar_deveRetornar200() {
        PageResponse<ProdutoResponse> page = new PageResponse<>(
                List.of(response), 0, 10, 1, 1, 1, true, true, false, false, false
        );
        when(produtoService.listar(0, 10, null)).thenReturn(page);

        ResponseEntity<PageResponse<ProdutoResponse>> result = produtoController.listar(0, 10, null);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().content()).hasSize(1);
    }

    @Test
    void atualizar_deveRetornar200() {
        when(produtoService.atualizar(eq(1L), any())).thenReturn(response);

        ResponseEntity<ProdutoResponse> result = produtoController.atualizar(1L, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void excluir_deveRetornar204() {
        doNothing().when(produtoService).excluirLogicamente(1L);

        ResponseEntity<Void> result = produtoController.excluir(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(produtoService).excluirLogicamente(1L);
    }
}
