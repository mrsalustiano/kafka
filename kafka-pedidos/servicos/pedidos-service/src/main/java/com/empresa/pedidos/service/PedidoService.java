package com.empresa.pedidos.service;

import com.empresa.pedidos.audit.AuditoriaService;
import com.empresa.pedidos.audit.OperacaoAuditoria;
import com.empresa.pedidos.dto.PageResponse;
import com.empresa.pedidos.dto.PedidoCreateEvent;
import com.empresa.pedidos.dto.PedidoRequest;
import com.empresa.pedidos.dto.PedidoResponse;
import com.empresa.pedidos.dto.PedidoStatusRequest;
import com.empresa.pedidos.dto.ProdutoCacheDto;
import com.empresa.pedidos.entity.Pedido;
import com.empresa.pedidos.exception.DatabaseException;
import com.empresa.pedidos.exception.NotFoundException;
import com.empresa.pedidos.exception.ValidationException;
import com.empresa.pedidos.mapper.PedidoMapper;
import com.empresa.pedidos.producer.PedidoCreateProducer;
import com.empresa.pedidos.repository.ClienteRepository;
import com.empresa.pedidos.repository.PedidoRepository;
import com.empresa.pedidos.entity.Cliente;
import com.empresa.pedidos.util.CorrelationIdUtil;
import com.empresa.pedidos.util.CpfUtil;
import com.empresa.pedidos.util.PageResponseUtil;
import com.empresa.pedidos.util.PedidoStatusUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoConsultaService produtoConsultaService;
    private final PedidoMapper pedidoMapper;
    private final PedidoCreateProducer pedidoCreateProducer;
    private final AuditoriaService auditoriaService;

    @Transactional
    public PedidoResponse criar(PedidoRequest request) {
        try {
            Cliente cliente = clienteRepository.findAtivoById(request.codigoCliente())
                    .orElseThrow(() -> new ValidationException(
                            "Cliente inativo ou nao encontrado: " + request.codigoCliente()));
            if (!CpfUtil.isValido(cliente.getCpf())) {
                throw new ValidationException("CPF do cliente invalido: " + request.codigoCliente());
            }

            ProdutoCacheDto produto = produtoConsultaService.buscarProdutoAtivo(request.codigoProduto());

            Pedido pedido = Pedido.builder()
                    .dataPedido(LocalDateTime.now())
                    .codigoCliente(request.codigoCliente())
                    .codigoProduto(request.codigoProduto())
                    .valorUnitario(produto.valor())
                    .quantidade(request.quantidade())
                    .status(PedidoStatusUtil.EM_PROCESSAMENTO)
                    .build();

            long codigoPedido = pedidoRepository.insert(pedido);
            String eventId = UUID.randomUUID().toString();

            PedidoCreateEvent event = new PedidoCreateEvent(
                    eventId,
                    CorrelationIdUtil.get(),
                    codigoPedido,
                    request.codigoCliente(),
                    request.codigoProduto(),
                    produto.valor(),
                    request.quantidade(),
                    PedidoStatusUtil.EM_PROCESSAMENTO
            );

            pedidoCreateProducer.publicar(event);

            Pedido salvo = pedidoRepository.findById(codigoPedido)
                    .orElseThrow(() -> new NotFoundException("Pedido nao encontrado apos criacao"));
            auditoriaService.registrar(
                    OperacaoAuditoria.CREATE,
                    codigoPedido,
                    "Pedido criado: eventId=" + eventId
            );
            log.info("Pedido criado: codigoPedido={}, eventId={}", codigoPedido, eventId);
            return pedidoMapper.toResponse(salvo);
        } catch (ValidationException | NotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DatabaseException("Erro ao criar pedido", ex);
        }
    }

    public PedidoResponse buscarPorId(Long id) {
        try {
            Pedido pedido = pedidoRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Pedido nao encontrado: " + id));
            return pedidoMapper.toResponse(pedido);
        } catch (NotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DatabaseException("Erro ao buscar pedido: " + id, ex);
        }
    }

    public PageResponse<PedidoResponse> listar(int page, int size, String sort) {
        try {
            String sortColumn = PageResponseUtil.resolveSortColumn(sort);
            String sortDirection = PageResponseUtil.resolveSortDirection(sort);
            long offset = (long) page * size;
            List<Pedido> pedidos = pedidoRepository.findAllPaginado(sortColumn, sortDirection, size, offset);
            long total = pedidoRepository.count();
            List<PedidoResponse> content = pedidoMapper.toResponseList(pedidos);
            return PageResponseUtil.of(content, page, size, total);
        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DatabaseException("Erro ao listar pedidos", ex);
        }
    }

    @Transactional
    public PedidoResponse atualizar(Long id, PedidoRequest request) {
        try {
            Pedido existente = pedidoRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Pedido nao encontrado: " + id));

            Cliente cliente = clienteRepository.findAtivoById(request.codigoCliente())
                    .orElseThrow(() -> new ValidationException(
                            "Cliente inativo ou nao encontrado: " + request.codigoCliente()));
            if (!CpfUtil.isValido(cliente.getCpf())) {
                throw new ValidationException("CPF do cliente invalido: " + request.codigoCliente());
            }
            produtoConsultaService.buscarProdutoAtivo(request.codigoProduto());

            existente.setCodigoCliente(request.codigoCliente());
            existente.setCodigoProduto(request.codigoProduto());
            existente.setQuantidade(request.quantidade());

            int atualizados = pedidoRepository.update(existente);
            if (atualizados == 0) {
                throw new NotFoundException("Pedido nao encontrado: " + id);
            }

            Pedido atualizado = pedidoRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Pedido nao encontrado: " + id));
            auditoriaService.registrar(OperacaoAuditoria.UPDATE, id, "Pedido atualizado");
            return pedidoMapper.toResponse(atualizado);
        } catch (NotFoundException | ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DatabaseException("Erro ao atualizar pedido: " + id, ex);
        }
    }

    @Transactional
    public PedidoResponse atualizarStatus(Long id, PedidoStatusRequest request) {
        try {
            pedidoRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Pedido nao encontrado: " + id));

            PedidoStatusUtil.validar(request.status());

            int atualizados = pedidoRepository.atualizarStatus(id, request.status());
            if (atualizados == 0) {
                throw new NotFoundException("Pedido nao encontrado: " + id);
            }

            Pedido atualizado = pedidoRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Pedido nao encontrado: " + id));
            auditoriaService.registrar(
                    OperacaoAuditoria.UPDATE,
                    id,
                    "Status do pedido atualizado para " + request.status()
            );
            return pedidoMapper.toResponse(atualizado);
        } catch (NotFoundException | ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DatabaseException("Erro ao atualizar status do pedido: " + id, ex);
        }
    }
}
