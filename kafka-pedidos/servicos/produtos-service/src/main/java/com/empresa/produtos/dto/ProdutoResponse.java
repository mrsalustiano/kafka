package com.empresa.produtos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProdutoResponse(
        Long codigoProduto,
        String descricao,
        BigDecimal valor,
        String ativo,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}
