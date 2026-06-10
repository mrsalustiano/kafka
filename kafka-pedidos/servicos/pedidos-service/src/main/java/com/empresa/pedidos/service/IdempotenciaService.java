package com.empresa.pedidos.service;

import com.empresa.pedidos.exception.DatabaseException;
import com.empresa.pedidos.repository.MensagemProcessadaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdempotenciaService {

    private final MensagemProcessadaRepository mensagemProcessadaRepository;

    public boolean jaProcessado(String eventId) {
        try {
            return mensagemProcessadaRepository.existsByEventId(eventId);
        } catch (Exception ex) {
            throw new DatabaseException("Erro ao verificar idempotencia do evento: " + eventId, ex);
        }
    }

    public void registrar(String eventId) {
        try {
            mensagemProcessadaRepository.insert(eventId);
        } catch (Exception ex) {
            throw new DatabaseException("Erro ao registrar idempotencia do evento: " + eventId, ex);
        }
    }
}
