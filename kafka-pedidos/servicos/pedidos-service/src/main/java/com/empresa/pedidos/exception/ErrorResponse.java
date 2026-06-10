package com.empresa.pedidos.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String message,
        String path,
        String correlationId
) {
}
