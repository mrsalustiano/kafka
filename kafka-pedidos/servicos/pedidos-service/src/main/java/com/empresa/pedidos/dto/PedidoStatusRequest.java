package com.empresa.pedidos.dto;

import jakarta.validation.constraints.NotBlank;

public record PedidoStatusRequest(
        @NotBlank(message = "status e obrigatorio")
        String status
) {
}
