package com.empresa.pedidos.util;

import com.empresa.pedidos.exception.ValidationException;

import java.util.Set;

public final class PedidoStatusUtil {

    public static final String EM_PROCESSAMENTO = "EM_PROCESSAMENTO";

    private static final Set<String> STATUS_VALIDOS = Set.of(
            "EM_PROCESSAMENTO",
            "FINALIZADO",
            "EM_SEPARACAO",
            "ENTREGUE",
            "CANCELADO",
            "EM_ROTA_DE_ENTREGA"
    );

    private PedidoStatusUtil() {
    }

    public static void validar(String status) {
        if (status == null || status.isBlank()) {
            throw new ValidationException("Status e obrigatorio");
        }
        if (!STATUS_VALIDOS.contains(status)) {
            throw new ValidationException("Status invalido: " + status);
        }
    }
}
