package com.empresa.broker.service;

import com.empresa.broker.audit.AuditoriaService;
import com.empresa.broker.audit.OperacaoAuditoria;
import com.empresa.broker.dto.ClienteCreateEvent;
import com.empresa.broker.dto.ClienteResponseEvent;
import com.empresa.broker.entity.Cliente;
import com.empresa.broker.exception.BusinessException;
import com.empresa.broker.exception.DatabaseException;
import com.empresa.broker.exception.KafkaPublishException;
import com.empresa.broker.exception.TimeoutException;
import com.empresa.broker.exception.ValidationException;
import com.empresa.broker.producer.ClienteResponseProducer;
import com.empresa.broker.repository.ClienteRepository;
import com.empresa.broker.util.CpfUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteProcessamentoService {

    private static final String STATUS_CRIADO = "CRIADO";
    private static final String ATIVO = "S";

    private final IdempotenciaService idempotenciaService;
    private final ClienteRepository clienteRepository;
    private final ClienteResponseProducer clienteResponseProducer;
    private final AuditoriaService auditoriaService;

    @Transactional
    public void processar(ClienteCreateEvent event) {
        if (event.eventId() == null || event.eventId().isBlank()) {
            log.warn("Evento client-create ignorado: eventId ausente");
            return;
        }

        if (idempotenciaService.jaProcessado(event.eventId())) {
            log.warn("Evento duplicado ignorado: eventId={}", event.eventId());
            return;
        }

        try {
            String cpf = CpfUtil.validarENormalizar(event.cpf());
            if (clienteRepository.existsAtivoByCpf(cpf)) {
                throw new BusinessException("CPF ja cadastrado: " + cpf);
            }

            Cliente cliente = Cliente.builder()
                    .nome(event.nome())
                    .cpf(cpf)
                    .endereco(event.endereco())
                    .cep(event.cep())
                    .cidade(event.cidade())
                    .estado(event.estado())
                    .email(event.email())
                    .telefone(event.telefone())
                    .ativo(ATIVO)
                    .build();

            long codigoCliente = clienteRepository.insert(cliente);
            log.info("Cliente persistido com id {}", codigoCliente);

            ClienteResponseEvent response = new ClienteResponseEvent(
                    event.eventId(), codigoCliente, STATUS_CRIADO);

            clienteResponseProducer.publicar(response);
            idempotenciaService.registrar(event.eventId());

            auditoriaService.registrar(
                    OperacaoAuditoria.PROCESSAMENTO_KAFKA,
                    codigoCliente,
                    "Cliente criado via broker, eventId=" + event.eventId() + ", cpf=" + cpf
            );
        } catch (ValidationException | BusinessException | KafkaPublishException | TimeoutException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new DatabaseException("Erro ao processar evento client-create: " + event.eventId(), ex);
        }
    }
}
