package com.empresa.pedidos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProdutoCacheDto(
        Long codigoProduto,
        String descricao,
        BigDecimal valor,
        String ativo,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}
