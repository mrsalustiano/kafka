package com.empresa.pedidos.service;

import com.empresa.pedidos.audit.AuditoriaService;
import com.empresa.pedidos.audit.OperacaoAuditoria;
import com.empresa.pedidos.dto.PageResponse;
import com.empresa.pedidos.dto.PedidoRequest;
import com.empresa.pedidos.dto.PedidoResponse;
import com.empresa.pedidos.dto.PedidoStatusRequest;
import com.empresa.pedidos.dto.ProdutoCacheDto;
import com.empresa.pedidos.entity.Cliente;
import com.empresa.pedidos.entity.Pedido;
import com.empresa.pedidos.exception.DatabaseException;
import com.empresa.pedidos.exception.NotFoundException;
import com.empresa.pedidos.exception.ValidationException;
import com.empresa.pedidos.mapper.PedidoMapper;
import com.empresa.pedidos.producer.PedidoCreateProducer;
import com.empresa.pedidos.repository.ClienteRepository;
import com.empresa.pedidos.repository.PedidoRepository;
import com.empresa.pedidos.util.CorrelationIdUtil;
import org.junit.jupiter.api.AfterEach;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ProdutoConsultaService produtoConsultaService;
    @Mock
    private PedidoMapper pedidoMapper;
    @Mock
    private PedidoCreateProducer pedidoCreateProducer;
    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private PedidoService pedidoService;

    private Pedido pedido;
    private PedidoResponse pedidoResponse;
    private PedidoRequest pedidoRequest;
    private ProdutoCacheDto produtoCacheDto;

    @BeforeEach
    void setUp() {
        pedido = Pedido.builder()
                .codigoPedido(1L)
                .dataPedido(LocalDateTime.now())
                .codigoCliente(1L)
                .codigoProduto(2L)
                .valorUnitario(BigDecimal.TEN)
                .quantidade(3)
                .status("EM_PROCESSAMENTO")
                .dataCriacao(LocalDateTime.now())
                .build();
        pedidoResponse = new PedidoResponse(
                1L, pedido.getDataPedido(), 1L, 2L, BigDecimal.TEN, 3, "EM_PROCESSAMENTO", pedido.getDataCriacao());
        pedidoRequest = new PedidoRequest(1L, 2L, 3);
        produtoCacheDto = new ProdutoCacheDto(2L, "Produto", BigDecimal.TEN, "S", LocalDateTime.now(), null);
        CorrelationIdUtil.set("corr-1");
    }

    @AfterEach
    void tearDown() {
        CorrelationIdUtil.clear();
    }

    @Test
    void criar_quandoSucesso_devePersistirPublicarEAuditar() {
        when(clienteRepository.findAtivoById(1L)).thenReturn(Optional.of(Cliente.builder().codigoCliente(1L).ativo("S").build()));
        when(produtoConsultaService.buscarProdutoAtivo(2L)).thenReturn(produtoCacheDto);
        when(pedidoRepository.insert(any())).thenReturn(1L);
        doNothing().when(pedidoCreateProducer).publicar(any());
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoMapper.toResponse(pedido)).thenReturn(pedidoResponse);

        PedidoResponse result = pedidoService.criar(pedidoRequest);

        assertThat(result).isEqualTo(pedidoResponse);
        verify(pedidoCreateProducer).publicar(any());
        verify(auditoriaService).registrar(eq(OperacaoAuditoria.CREATE), eq(1L), anyString());
    }

    @Test
    void criar_quandoClienteInativo_deveLancarValidationException() {
        when(clienteRepository.findAtivoById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.criar(pedidoRequest))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void criar_quandoErroDb_deveLancarDatabaseException() {
        when(clienteRepository.findAtivoById(1L)).thenReturn(Optional.of(Cliente.builder().codigoCliente(1L).ativo("S").build()));
        when(produtoConsultaService.buscarProdutoAtivo(2L)).thenReturn(produtoCacheDto);
        when(pedidoRepository.insert(any())).thenThrow(new RuntimeException("erro"));

        assertThatThrownBy(() -> pedidoService.criar(pedidoRequest))
                .isInstanceOf(DatabaseException.class);
    }

    @Test
    void buscarPorId_quandoEncontrado_deveRetornar() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoMapper.toResponse(pedido)).thenReturn(pedidoResponse);

        assertThat(pedidoService.buscarPorId(1L)).isEqualTo(pedidoResponse);
    }

    @Test
    void buscarPorId_quandoNaoEncontrado_deveLancarNotFoundException() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.buscarPorId(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void listar_deveRetornarPaginado() {
        when(pedidoRepository.findAllPaginado(anyString(), anyString(), anyInt(), anyLong()))
                .thenReturn(List.of(pedido));
        when(pedidoRepository.count()).thenReturn(1L);
        when(pedidoMapper.toResponseList(List.of(pedido))).thenReturn(List.of(pedidoResponse));

        PageResponse<PedidoResponse> result = pedidoService.listar(0, 10, null);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void listar_quandoSortInvalido_deveLancarValidationException() {
        assertThatThrownBy(() -> pedidoService.listar(0, 10, "campo_invalido,ASC"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void atualizar_quandoSucesso_deveAtualizar() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(clienteRepository.findAtivoById(1L)).thenReturn(Optional.of(Cliente.builder().codigoCliente(1L).ativo("S").build()));
        when(produtoConsultaService.buscarProdutoAtivo(2L)).thenReturn(produtoCacheDto);
        when(pedidoRepository.update(any())).thenReturn(1);
        when(pedidoMapper.toResponse(pedido)).thenReturn(pedidoResponse);

        assertThat(pedidoService.atualizar(1L, pedidoRequest)).isEqualTo(pedidoResponse);
    }

    @Test
    void atualizar_quandoNaoEncontrado_deveLancarNotFoundException() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.atualizar(1L, pedidoRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void atualizarStatus_quandoSucesso_deveAtualizar() {
        PedidoStatusRequest statusRequest = new PedidoStatusRequest("FINALIZADO");
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.atualizarStatus(1L, "FINALIZADO")).thenReturn(1);
        when(pedidoMapper.toResponse(pedido)).thenReturn(pedidoResponse);

        assertThat(pedidoService.atualizarStatus(1L, statusRequest)).isEqualTo(pedidoResponse);
    }

    @Test
    void atualizarStatus_quandoStatusInvalido_deveLancarValidationException() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThatThrownBy(() -> pedidoService.atualizarStatus(1L, new PedidoStatusRequest("INVALIDO")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void atualizarStatus_quandoNaoEncontrado_deveLancarNotFoundException() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pedidoService.atualizarStatus(1L, new PedidoStatusRequest("FINALIZADO")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void buscarPorId_quandoErroDb_deveLancarDatabaseException() {
        when(pedidoRepository.findById(1L)).thenThrow(new RuntimeException("erro"));

        assertThatThrownBy(() -> pedidoService.buscarPorId(1L))
                .isInstanceOf(DatabaseException.class);
    }

    @Test
    void listar_quandoErroDb_deveLancarDatabaseException() {
        when(pedidoRepository.findAllPaginado(anyString(), anyString(), anyInt(), anyLong()))
                .thenThrow(new RuntimeException("erro"));

        assertThatThrownBy(() -> pedidoService.listar(0, 10, null))
                .isInstanceOf(DatabaseException.class);
    }

    @Test
    void atualizar_quandoUpdateRetornaZero_deveLancarNotFoundException() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(clienteRepository.findAtivoById(1L)).thenReturn(Optional.of(Cliente.builder().codigoCliente(1L).ativo("S").build()));
        when(produtoConsultaService.buscarProdutoAtivo(2L)).thenReturn(produtoCacheDto);
        when(pedidoRepository.update(any())).thenReturn(0);

        assertThatThrownBy(() -> pedidoService.atualizar(1L, pedidoRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void atualizarStatus_quandoUpdateRetornaZero_deveLancarNotFoundException() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.atualizarStatus(1L, "FINALIZADO")).thenReturn(0);

        assertThatThrownBy(() -> pedidoService.atualizarStatus(1L, new PedidoStatusRequest("FINALIZADO")))
                .isInstanceOf(NotFoundException.class);
    }
}
