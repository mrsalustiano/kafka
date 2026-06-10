package com.empresa.clientes.dto;

import java.time.LocalDateTime;

public record ClienteResponse(
        Long codigoCliente,
        String nome,
        String cpf,
        String endereco,
        String cep,
        String cidade,
        String estado,
        String email,
        String telefone,
        String ativo,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
}
