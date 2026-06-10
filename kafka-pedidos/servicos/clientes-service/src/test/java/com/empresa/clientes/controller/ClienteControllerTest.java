package com.empresa.clientes.controller;

import com.empresa.clientes.dto.ClienteAcceptedResponse;
import com.empresa.clientes.dto.ClienteRequest;
import com.empresa.clientes.dto.ClienteResponse;
import com.empresa.clientes.dto.ClienteStatusResponse;
import com.empresa.clientes.dto.PageResponse;
import com.empresa.clientes.exception.NotFoundException;
import com.empresa.clientes.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
class ClienteControllerTest {

    @Mock
    private ClienteService clienteService;

    @InjectMocks
    private ClienteController clienteController;

    private static final String CPF_VALIDO = "52998224725";

    private final ClienteRequest request = new ClienteRequest("Cliente", CPF_VALIDO, "Rua 1", "12345-000",
            "Sao Paulo", "SP", "a@test.com", "11999999999");
    private final ClienteResponse response = new ClienteResponse(1L, "Cliente", CPF_VALIDO, "Rua 1", "12345-000",
            "Sao Paulo", "SP", "a@test.com", "11999999999", "S", LocalDateTime.now(), null);
    private final ClienteAcceptedResponse acceptedResponse = new ClienteAcceptedResponse("evt-1", "corr-1", "ACEITO");
    private final ClienteStatusResponse statusResponse = new ClienteStatusResponse(1L, "CRIADO", LocalDateTime.now());

    @Test
    void solicitarCriacao_deveRetornar202() {
        when(clienteService.solicitarCriacao(request)).thenReturn(acceptedResponse);

        ResponseEntity<ClienteAcceptedResponse> result = clienteController.solicitarCriacao(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(result.getBody()).isEqualTo(acceptedResponse);
    }

    @Test
    void buscarPorCpf_deveRetornar200() {
        when(clienteService.buscarPorCpf(CPF_VALIDO)).thenReturn(response);

        ResponseEntity<ClienteResponse> result = clienteController.buscarPorCpf(CPF_VALIDO);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void buscarPorId_deveRetornar200() {
        when(clienteService.buscarPorId(1L)).thenReturn(response);

        ResponseEntity<ClienteResponse> result = clienteController.buscarPorId(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void buscarPorId_quandoNaoEncontrado_devePropagarExcecao() {
        when(clienteService.buscarPorId(1L)).thenThrow(new NotFoundException("nao encontrado"));

        assertThatThrownBy(() -> clienteController.buscarPorId(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void listar_deveRetornar200() {
        PageResponse<ClienteResponse> page = new PageResponse<>(
                List.of(response), 0, 10, 1, 1, 1, true, true, false, false, false
        );
        when(clienteService.listar(0, 10, null)).thenReturn(page);

        ResponseEntity<PageResponse<ClienteResponse>> result = clienteController.listar(0, 10, null);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().content()).hasSize(1);
    }

    @Test
    void atualizar_deveRetornar200() {
        when(clienteService.atualizar(eq(1L), any())).thenReturn(response);

        ResponseEntity<ClienteResponse> result = clienteController.atualizar(1L, request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void excluir_deveRetornar204() {
        doNothing().when(clienteService).excluirLogicamente(1L);

        ResponseEntity<Void> result = clienteController.excluir(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(clienteService).excluirLogicamente(1L);
    }

    @Test
    void consultarStatus_deveRetornar200() {
        when(clienteService.consultarStatus(1L)).thenReturn(statusResponse);

        ResponseEntity<ClienteStatusResponse> result = clienteController.consultarStatus(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(statusResponse);
    }
}
