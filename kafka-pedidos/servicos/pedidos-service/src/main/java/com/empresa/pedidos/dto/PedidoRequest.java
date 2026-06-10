package com.empresa.pedidos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PedidoRequest(
        @NotNull(message = "codigoCliente e obrigatorio")
        Long codigoCliente,

        @NotNull(message = "codigoProduto e obrigatorio")
        Long codigoProduto,

        @NotNull(message = "quantidade e obrigatoria")
        @Min(value = 1, message = "quantidade deve ser maior que zero")
        Integer quantidade
) {
}
