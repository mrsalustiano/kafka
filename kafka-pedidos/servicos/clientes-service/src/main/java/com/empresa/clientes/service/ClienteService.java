package com.empresa.clientes.service;

import com.empresa.clientes.audit.AuditoriaService;
import com.empresa.clientes.audit.OperacaoAuditoria;
import com.empresa.clientes.dto.ClienteAcceptedResponse;
import com.empresa.clientes.dto.ClienteCreateEvent;
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
import com.empresa.clientes.util.CpfUtil;
import com.empresa.clientes.util.PageResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteService {

    private static final String STATUS_ACEITO = "ACEITO";

    private final ClienteRepository clienteRepository;
    private final ClienteStatusRepository clienteStatusRepository;
    private final ClienteMapper clienteMapper;
    private final ClienteCreateProducer clienteCreateProducer;
    private final AuditoriaService auditoriaService;

    public ClienteAcceptedResponse solicitarCriacao(ClienteRequest request) {
        String cpf = CpfUtil.validarENormalizar(request.cpf());
        validarCpfUnico(cpf, null);

        String eventId = UUID.randomUUID().toString();
        String correlationId = CorrelationIdUtil.get();

        ClienteCreateEvent event = new ClienteCreateEvent(
                eventId,
                correlationId,
                request.nome(),
                cpf,
                request.endereco(),
                request.cep(),
                request.cidade(),
                request.estado(),
                request.email(),
                request.telefone()
        );

        clienteCreateProducer.publicar(event);
        auditoriaService.registrar(
                OperacaoAuditoria.CREATE,
                null,
                "Solicitacao de criacao de cliente: " + request.nome() + ", cpf=" + cpf + ", eventId=" + eventId
        );

        log.info("Solicitacao de criacao de cliente aceita: eventId={}, nome={}, cpf={}", eventId, request.nome(), cpf);
        return new ClienteAcceptedResponse(eventId, correlationId, STATUS_ACEITO);
    }

    public ClienteResponse buscarPorId(Long id) {
        try {
            Cliente cliente = clienteRepository.findAtivoById(id)
                    .orElseThrow(() -> new NotFoundException("Cliente nao encontrado: " + id));
            return clienteMapper.toResponse(cliente);
        } catch (NotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DatabaseException("Erro ao buscar cliente: " + id, ex);
        }
    }

    public ClienteResponse buscarPorCpf(String cpf) {
        try {
            String cpfNormalizado = CpfUtil.validarENormalizar(cpf);
            Cliente cliente = clienteRepository.findAtivoByCpf(cpfNormalizado)
                    .orElseThrow(() -> new NotFoundException("Cliente nao encontrado para CPF: " + cpfNormalizado));
            return clienteMapper.toResponse(cliente);
        } catch (NotFoundException | ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DatabaseException("Erro ao buscar cliente por CPF: " + cpf, ex);
        }
    }

    public PageResponse<ClienteResponse> listar(int page, int size, String sort) {
        try {
            String sortColumn = PageResponseUtil.resolveSortColumn(sort);
            String sortDirection = PageResponseUtil.resolveSortDirection(sort);
            long offset = (long) page * size;
            List<Cliente> clientes = clienteRepository.findAllAtivosPaginado(sortColumn, sortDirection, size, offset);
            long total = clienteRepository.countAtivos();
            List<ClienteResponse> content = clienteMapper.toResponseList(clientes);
            return PageResponseUtil.of(content, page, size, total);
        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DatabaseException("Erro ao listar clientes", ex);
        }
    }

    @Transactional
    public ClienteResponse atualizar(Long id, ClienteRequest request) {
        try {
            Cliente existente = clienteRepository.findAtivoById(id)
                    .orElseThrow(() -> new NotFoundException("Cliente nao encontrado: " + id));

            String cpf = CpfUtil.validarENormalizar(request.cpf());
            validarCpfUnico(cpf, id);

            existente.setNome(request.nome());
            existente.setCpf(cpf);
            existente.setEndereco(request.endereco());
            existente.setCep(request.cep());
            existente.setCidade(request.cidade());
            existente.setEstado(request.estado());
            existente.setEmail(request.email());
            existente.setTelefone(request.telefone());

            int atualizados = clienteRepository.update(existente);
            if (atualizados == 0) {
                throw new NotFoundException("Cliente nao encontrado: " + id);
            }

            Cliente atualizado = clienteRepository.findAtivoById(id)
                    .orElseThrow(() -> new NotFoundException("Cliente nao encontrado: " + id));
            ClienteResponse response = clienteMapper.toResponse(atualizado);
            auditoriaService.registrar(OperacaoAuditoria.UPDATE, id, "Cliente atualizado: " + request.nome());
            return response;
        } catch (NotFoundException | ValidationException | BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DatabaseException("Erro ao atualizar cliente: " + id, ex);
        }
    }

    @Transactional
    public void excluirLogicamente(Long id) {
        try {
            clienteRepository.findAtivoById(id)
                    .orElseThrow(() -> new NotFoundException("Cliente nao encontrado: " + id));

            int desativados = clienteRepository.desativar(id);
            if (desativados == 0) {
                throw new NotFoundException("Cliente nao encontrado: " + id);
            }

            auditoriaService.registrar(OperacaoAuditoria.DELETE, id, "Exclusao logica do cliente");
        } catch (NotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DatabaseException("Erro ao excluir cliente: " + id, ex);
        }
    }

    public ClienteStatusResponse consultarStatus(Long id) {
        try {
            ClienteStatus status = clienteStatusRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Status do cliente nao encontrado: " + id));
            return clienteMapper.toStatusResponse(status);
        } catch (NotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DatabaseException("Erro ao consultar status do cliente: " + id, ex);
        }
    }

    private void validarCpfUnico(String cpf, Long codigoClienteExcluir) {
        boolean existe = codigoClienteExcluir == null
                ? clienteRepository.existsAtivoByCpf(cpf)
                : clienteRepository.existsAtivoByCpfAndNotId(cpf, codigoClienteExcluir);
        if (existe) {
            throw new BusinessException("CPF ja cadastrado: " + cpf);
        }
    }
}
