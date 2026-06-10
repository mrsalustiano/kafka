package com.empresa.clientes.service;

import com.empresa.clientes.audit.AuditoriaService;
import com.empresa.clientes.audit.OperacaoAuditoria;
import com.empresa.clientes.dto.ClienteAcceptedResponse;
import com.empresa.clientes.dto.ClienteRequest;
import com.empresa.clientes.dto.ClienteResponse;
import com.empresa.clientes.dto.ClienteStatusResponse;
import com.empresa.clientes.dto.PageResponse;
import com.empresa.clientes.entity.Cliente;
import com.empresa.clientes.entity.ClienteStatus;
import com.empresa.clientes.exception.BusinessException;
import com.empresa.clientes.exception.DatabaseException;
import com.empresa.clientes.exception.NotFoundException;
import com.empresa.clientes.exception.ValidationException;
import com.empresa.clientes.mapper.ClienteMapper;
import com.empresa.clientes.producer.ClienteCreateProducer;
import com.empresa.clientes.repository.ClienteRepository;
import com.empresa.clientes.repository.ClienteStatusRepository;
import com.empresa.clientes.util.CorrelationIdUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    private static final String CPF_VALIDO = "52998224725";

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteStatusRepository clienteStatusRepository;

    @Mock
    private ClienteMapper clienteMapper;

    @Mock
    private ClienteCreateProducer clienteCreateProducer;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente;
    private ClienteResponse clienteResponse;
    private ClienteRequest clienteRequest;
    private ClienteStatus clienteStatus;
    private ClienteStatusResponse clienteStatusResponse;

    @BeforeEach
    void setUp() {
        cliente = Cliente.builder()
                .codigoCliente(1L)
                .nome("Cliente A")
                .cpf(CPF_VALIDO)
                .endereco("Rua 1")
                .cep("12345-000")
                .cidade("Sao Paulo")
                .estado("SP")
                .email("a@test.com")
                .telefone("11999999999")
                .ativo("S")
                .dataCriacao(LocalDateTime.now())
                .build();

        clienteResponse = new ClienteResponse(1L, "Cliente A", CPF_VALIDO, "Rua 1", "12345-000", "Sao Paulo", "SP",
                "a@test.com", "11999999999", "S", LocalDateTime.now(), null);

        clienteRequest = new ClienteRequest("Cliente A", CPF_VALIDO, "Rua 1", "12345-000", "Sao Paulo", "SP",
                "a@test.com", "11999999999");

        clienteStatus = new ClienteStatus(1L, "CRIADO", LocalDateTime.now());
        clienteStatusResponse = new ClienteStatusResponse(1L, "CRIADO", LocalDateTime.now());
    }

    @AfterEach
    void tearDown() {
        CorrelationIdUtil.clear();
    }

    @Test
    void solicitarCriacao_devePublicarEventoERetornarAceito() {
        CorrelationIdUtil.set("corr-1");
        when(clienteRepository.existsAtivoByCpf(CPF_VALIDO)).thenReturn(false);
        doNothing().when(clienteCreateProducer).publicar(any());

        ClienteAcceptedResponse result = clienteService.solicitarCriacao(clienteRequest);

        assertThat(result.correlationId()).isEqualTo("corr-1");
        assertThat(result.status()).isEqualTo("ACEITO");
        assertThat(result.eventId()).isNotBlank();
        verify(clienteCreateProducer).publicar(any());
        verify(auditoriaService).registrar(eq(OperacaoAuditoria.CREATE), eq(null), contains("Cliente A"));
    }

    @Test
    void solicitarCriacao_quandoCpfDuplicado_deveLancarBusinessException() {
        when(clienteRepository.existsAtivoByCpf(CPF_VALIDO)).thenReturn(true);

        assertThatThrownBy(() -> clienteService.solicitarCriacao(clienteRequest))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void solicitarCriacao_quandoCpfInvalido_deveLancarValidationException() {
        ClienteRequest requestInvalido = new ClienteRequest("Cliente A", "11111111111", "Rua 1", "12345-000",
                "Sao Paulo", "SP", "a@test.com", "11999999999");

        assertThatThrownBy(() -> clienteService.solicitarCriacao(requestInvalido))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void buscarPorCpf_deveRetornarCliente() {
        when(clienteRepository.findAtivoByCpf(CPF_VALIDO)).thenReturn(Optional.of(cliente));
        when(clienteMapper.toResponse(cliente)).thenReturn(clienteResponse);

        ClienteResponse result = clienteService.buscarPorCpf(CPF_VALIDO);

        assertThat(result).isEqualTo(clienteResponse);
    }

    @Test
    void buscarPorCpf_quandoNaoExiste_deveLancarNotFoundException() {
        when(clienteRepository.findAtivoByCpf(CPF_VALIDO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarPorCpf(CPF_VALIDO))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void buscarPorId_deveRetornarCliente() {
        when(clienteRepository.findAtivoById(1L)).thenReturn(Optional.of(cliente));
        when(clienteMapper.toResponse(cliente)).thenReturn(clienteResponse);

        ClienteResponse result = clienteService.buscarPorId(1L);

        assertThat(result).isEqualTo(clienteResponse);
    }

    @Test
    void buscarPorId_quandoNaoExiste_deveLancarNotFoundException() {
        when(clienteRepository.findAtivoById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarPorId(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void buscarPorId_quandoErroBanco_deveLancarDatabaseException() {
        when(clienteRepository.findAtivoById(1L)).thenThrow(new RuntimeException("erro"));

        assertThatThrownBy(() -> clienteService.buscarPorId(1L))
                .isInstanceOf(DatabaseException.class);
    }

    @Test
    void listar_deveRetornarPagina() {
        when(clienteRepository.findAllAtivosPaginado("codigo_cliente", "ASC", 10, 0))
                .thenReturn(List.of(cliente));
        when(clienteRepository.countAtivos()).thenReturn(1L);
        when(clienteMapper.toResponseList(List.of(cliente))).thenReturn(List.of(clienteResponse));

        PageResponse<ClienteResponse> result = clienteService.listar(0, 10, null);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void listar_comSortInvalido_deveLancarValidationException() {
        assertThatThrownBy(() -> clienteService.listar(0, 10, "campo_invalido,asc"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void listar_quandoErroBanco_deveLancarDatabaseException() {
        when(clienteRepository.findAllAtivosPaginado(anyString(), anyString(), anyInt(), anyLong()))
                .thenThrow(new RuntimeException("erro"));

        assertThatThrownBy(() -> clienteService.listar(0, 10, null))
                .isInstanceOf(DatabaseException.class);
    }

    @Test
    void atualizar_deveRetornarClienteAtualizado() {
        when(clienteRepository.existsAtivoByCpfAndNotId(CPF_VALIDO, 1L)).thenReturn(false);

        Cliente atualizado = Cliente.builder()
                .codigoCliente(1L)
                .nome("Cliente A")
                .cpf(CPF_VALIDO)
                .endereco("Rua 1")
                .cep("12345-000")
                .cidade("Sao Paulo")
                .estado("SP")
                .email("a@test.com")
                .telefone("11999999999")
                .ativo("S")
                .build();

        when(clienteRepository.findAtivoById(1L))
                .thenReturn(Optional.of(cliente))
                .thenReturn(Optional.of(atualizado));
        when(clienteRepository.update(any(Cliente.class))).thenReturn(1);
        when(clienteMapper.toResponse(atualizado)).thenReturn(clienteResponse);

        ClienteResponse result = clienteService.atualizar(1L, clienteRequest);

        assertThat(result).isEqualTo(clienteResponse);
        verify(auditoriaService).registrar(OperacaoAuditoria.UPDATE, 1L, "Cliente atualizado: Cliente A");
    }

    @Test
    void atualizar_quandoNaoExiste_deveLancarNotFoundException() {
        when(clienteRepository.findAtivoById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.atualizar(1L, clienteRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void atualizar_quandoNenhumaLinhaAtualizada_deveLancarNotFoundException() {
        when(clienteRepository.findAtivoById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.update(any(Cliente.class))).thenReturn(0);

        assertThatThrownBy(() -> clienteService.atualizar(1L, clienteRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void atualizar_quandoNaoEncontradoAposUpdate_deveLancarNotFoundException() {
        when(clienteRepository.findAtivoById(1L))
                .thenReturn(Optional.of(cliente))
                .thenReturn(Optional.empty());
        when(clienteRepository.update(any(Cliente.class))).thenReturn(1);

        assertThatThrownBy(() -> clienteService.atualizar(1L, clienteRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void atualizar_quandoErroBanco_deveLancarDatabaseException() {
        when(clienteRepository.findAtivoById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.update(any(Cliente.class))).thenThrow(new RuntimeException("erro"));

        assertThatThrownBy(() -> clienteService.atualizar(1L, clienteRequest))
                .isInstanceOf(DatabaseException.class);
    }

    @Test
    void excluirLogicamente_deveDesativarCliente() {
        when(clienteRepository.findAtivoById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.desativar(1L)).thenReturn(1);

        clienteService.excluirLogicamente(1L);

        verify(auditoriaService).registrar(OperacaoAuditoria.DELETE, 1L, "Exclusao logica do cliente");
    }

    @Test
    void excluirLogicamente_quandoNaoExiste_deveLancarNotFoundException() {
        when(clienteRepository.findAtivoById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.excluirLogicamente(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void excluirLogicamente_quandoNenhumaLinhaDesativada_deveLancarNotFoundException() {
        when(clienteRepository.findAtivoById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.desativar(1L)).thenReturn(0);

        assertThatThrownBy(() -> clienteService.excluirLogicamente(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void excluirLogicamente_quandoErroBanco_deveLancarDatabaseException() {
        when(clienteRepository.findAtivoById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.desativar(1L)).thenThrow(new RuntimeException("erro"));

        assertThatThrownBy(() -> clienteService.excluirLogicamente(1L))
                .isInstanceOf(DatabaseException.class);
    }

    @Test
    void consultarStatus_deveRetornarStatus() {
        when(clienteStatusRepository.findById(1L)).thenReturn(Optional.of(clienteStatus));
        when(clienteMapper.toStatusResponse(clienteStatus)).thenReturn(clienteStatusResponse);

        ClienteStatusResponse result = clienteService.consultarStatus(1L);

        assertThat(result).isEqualTo(clienteStatusResponse);
    }

    @Test
    void consultarStatus_quandoNaoExiste_deveLancarNotFoundException() {
        when(clienteStatusRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.consultarStatus(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void consultarStatus_quandoErroBanco_deveLancarDatabaseException() {
        when(clienteStatusRepository.findById(1L)).thenThrow(new RuntimeException("erro"));

        assertThatThrownBy(() -> clienteService.consultarStatus(1L))
                .isInstanceOf(DatabaseException.class);
    }
}
