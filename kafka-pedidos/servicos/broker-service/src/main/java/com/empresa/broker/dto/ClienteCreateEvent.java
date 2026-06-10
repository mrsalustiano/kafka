package com.empresa.broker.dto;

public record ClienteCreateEvent(
        String eventId,
        String correlationId,
        String nome,
        String endereco,
        String cep,
        String cidade,
        String estado,
        String email,
        String telefone
) {
}
