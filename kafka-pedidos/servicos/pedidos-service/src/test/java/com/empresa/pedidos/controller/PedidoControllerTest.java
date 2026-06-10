package com.empresa.pedidos.controller;

import com.empresa.pedidos.dto.PageResponse;
import com.empresa.pedidos.dto.PedidoRequest;
import com.empresa.pedidos.dto.PedidoResponse;
import com.empresa.pedidos.dto.PedidoStatusRequest;
import com.empresa.pedidos.service.PedidoService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoControllerTest {

    @Mock
    private PedidoService pedidoService;

    @InjectMocks
    private PedidoController pedidoController;

    private final PedidoRequest request = new PedidoRequest(1L, 2L, 3);
    private final PedidoResponse response = new PedidoResponse(
            1L, LocalDateTime.now(), 1L, 2L, BigDecimal.TEN, 3, "EM_PROCESSAMENTO", LocalDateTime.now());

    @Test
    void criar_deveRetornar201() {
        when(pedidoService.criar(request)).thenReturn(response);

        ResponseEntity<PedidoResponse> result = pedidoController.criar(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void buscarPorId_deveRetornar200() {
        when(pedidoService.buscarPorId(1L)).thenReturn(response);

        ResponseEntity<PedidoResponse> result = pedidoController.buscarPorId(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void listar_deveRetornar200() {
        PageResponse<PedidoResponse> page = new PageResponse<>(
                List.of(response), 0, 10, 1, 1, 1, true, true, false, false, false);
        when(pedidoService.listar(0, 10, null)).thenReturn(page);

        ResponseEntity<PageResponse<PedidoResponse>> result = pedidoController.listar(0, 10, null);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(page);
    }

    @Test
    void atualizar_deveRetornar200() {
        when(pedidoService.atualizar(1L, request)).thenReturn(response);

        ResponseEntity<PedidoResponse> result = pedidoController.atualizar(1L, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(pedidoService).atualizar(1L, request);
    }

    @Test
    void atualizarStatus_deveRetornar200() {
        PedidoStatusRequest statusRequest = new PedidoStatusRequest("FINALIZADO");
        when(pedidoService.atualizarStatus(1L, statusRequest)).thenReturn(response);

        ResponseEntity<PedidoResponse> result = pedidoController.atualizarStatus(1L, statusRequest);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(pedidoService).atualizarStatus(1L, statusRequest);
    }
}
