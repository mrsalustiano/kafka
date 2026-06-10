package com.empresa.clientes.dto;

import com.empresa.clientes.validation.CpfValid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank(message = "nome e obrigatorio")
        @Size(max = 200, message = "nome deve ter no maximo 200 caracteres")
        String nome,

        @NotBlank(message = "cpf e obrigatorio")
        @CpfValid
        @Size(max = 14, message = "cpf deve ter no maximo 14 caracteres")
        String cpf,

        @Size(max = 255, message = "endereco deve ter no maximo 255 caracteres")
        String endereco,

        @Size(max = 20, message = "cep deve ter no maximo 20 caracteres")
        String cep,

        @Size(max = 100, message = "cidade deve ter no maximo 100 caracteres")
        String cidade,

        @Size(max = 2, message = "estado deve ter no maximo 2 caracteres")
        String estado,

        @Email(message = "email invalido")
        @Size(max = 150, message = "email deve ter no maximo 150 caracteres")
        String email,

        @Size(max = 30, message = "telefone deve ter no maximo 30 caracteres")
        String telefone
) {
}
