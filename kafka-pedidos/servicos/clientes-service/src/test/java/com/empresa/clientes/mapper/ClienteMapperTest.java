package com.empresa.clientes.mapper;

import com.empresa.clientes.dto.ClienteRequest;
import com.empresa.clientes.dto.ClienteResponse;
import com.empresa.clientes.dto.ClienteStatusResponse;
import com.empresa.clientes.entity.Cliente;
import com.empresa.clientes.entity.ClienteStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ClienteMapperTest {

    private final ClienteMapper mapper = Mappers.getMapper(ClienteMapper.class);

    @Test
    void toResponse_deveMapearCampos() {
        Cliente cliente = Cliente.builder()
                .codigoCliente(1L)
                .nome("Cliente")
                .cpf("52998224725")
                .endereco("Rua 1")
                .cep("12345-000")
                .cidade("Sao Paulo")
                .estado("SP")
                .email("a@test.com")
                .telefone("11999999999")
                .ativo("S")
                .dataCriacao(LocalDateTime.now())
                .build();

        ClienteResponse response = mapper.toResponse(cliente);

        assertThat(response.codigoCliente()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Cliente");
        assertThat(response.cpf()).isEqualTo("52998224725");
        assertThat(response.email()).isEqualTo("a@test.com");
    }

    @Test
    void toEntity_deveMapearRequest() {
        ClienteRequest request = new ClienteRequest("Novo", "52998224725", "Rua 2", "54321-000",
                "Rio", "RJ", "b@test.com", "21999999999");

        Cliente cliente = mapper.toEntity(request);

        assertThat(cliente.getNome()).isEqualTo("Novo");
        assertThat(cliente.getCpf()).isEqualTo("52998224725");
        assertThat(cliente.getAtivo()).isEqualTo("S");
        assertThat(cliente.getCodigoCliente()).isNull();
    }

    @Test
    void toResponseList_deveMapearLista() {
        Cliente cliente = Cliente.builder()
                .codigoCliente(1L)
                .nome("A")
                .ativo("S")
                .build();

        List<ClienteResponse> responses = mapper.toResponseList(List.of(cliente));

        assertThat(responses).hasSize(1);
    }

    @Test
    void toStatusResponse_deveMapearStatus() {
        ClienteStatus status = new ClienteStatus(1L, "CRIADO", LocalDateTime.now());

        ClienteStatusResponse response = mapper.toStatusResponse(status);

        assertThat(response.codigoCliente()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo("CRIADO");
    }

    @Test
    void toResponse_quandoNull_deveRetornarNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }

    @Test
    void toResponseList_quandoNull_deveRetornarNull() {
        assertThat(mapper.toResponseList(null)).isNull();
    }

    @Test
    void toStatusResponse_quandoNull_deveRetornarNull() {
        assertThat(mapper.toStatusResponse(null)).isNull();
    }

    @Test
    void toEntity_quandoNull_deveRetornarNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }
}
