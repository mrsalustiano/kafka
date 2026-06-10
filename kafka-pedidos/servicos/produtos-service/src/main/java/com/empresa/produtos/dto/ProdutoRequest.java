package com.empresa.produtos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProdutoRequest(
        @NotBlank(message = "descricao e obrigatoria")
        @Size(max = 255, message = "descricao deve ter no maximo 255 caracteres")
        String descricao,

        @NotNull(message = "valor e obrigatorio")
        @DecimalMin(value = "0.01", message = "valor deve ser maior que zero")
        BigDecimal valor
) {
}
