package com.empresa.pedidos.dto;

import java.math.BigDecimal;

public record PedidoCreateEvent(
        String eventId,
        String correlationId,
        Long codigoPedido,
        Long codigoCliente,
        Long codigoProduto,
        BigDecimal valorUnitario,
        Integer quantidade,
        String status
) {
}
