package com.empresa.pedidos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PedidoResponse(
        Long codigoPedido,
        LocalDateTime dataPedido,
        Long codigoCliente,
        Long codigoProduto,
        BigDecimal valorUnitario,
        Integer quantidade,
        String status,
        LocalDateTime dataCriacao
) {
}
