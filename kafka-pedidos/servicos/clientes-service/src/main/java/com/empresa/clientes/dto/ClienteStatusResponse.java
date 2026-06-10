package com.empresa.clientes.dto;

import java.time.LocalDateTime;

public record ClienteStatusResponse(
        Long codigoCliente,
        String status,
        LocalDateTime dataAtualizacao
) {
}
