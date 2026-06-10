package com.empresa.pedidos.dto;

public record PedidoResponseEvent(
        String eventId,
        Long codigoPedido,
        String status
) {
}
