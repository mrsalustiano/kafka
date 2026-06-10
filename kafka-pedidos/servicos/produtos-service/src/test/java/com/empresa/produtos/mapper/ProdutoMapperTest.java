package com.empresa.produtos.mapper;

import com.empresa.produtos.dto.ProdutoRequest;
import com.empresa.produtos.dto.ProdutoResponse;
import com.empresa.produtos.entity.Produto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProdutoMapperTest {

    private final ProdutoMapper mapper = Mappers.getMapper(ProdutoMapper.class);

    @Test
    void toResponse_deveMapearCampos() {
        Produto produto = Produto.builder()
                .codigoProduto(1L)
                .descricao("Produto")
                .valor(new BigDecimal("5.00"))
                .ativo("S")
                .dataCriacao(LocalDateTime.now())
                .build();

        ProdutoResponse response = mapper.toResponse(produto);

        assertThat(response.codigoProduto()).isEqualTo(1L);
        assertThat(response.descricao()).isEqualTo("Produto");
    }

    @Test
    void toEntity_deveMapearRequest() {
        ProdutoRequest request = new ProdutoRequest("Novo", new BigDecimal("12.00"));

        Produto produto = mapper.toEntity(request);

        assertThat(produto.getDescricao()).isEqualTo("Novo");
        assertThat(produto.getAtivo()).isEqualTo("S");
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
    void toEntity_quandoNull_deveRetornarNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toResponseList_deveMapearLista() {
        Produto produto = Produto.builder().codigoProduto(1L).descricao("A").valor(BigDecimal.ONE)
                .ativo("S").build();

        List<ProdutoResponse> responses = mapper.toResponseList(List.of(produto));

        assertThat(responses).hasSize(1);
    }
}
