package com.empresa.pedidos.mapper;

import com.empresa.pedidos.dto.PedidoResponse;
import com.empresa.pedidos.dto.ProdutoCacheDto;
import com.empresa.pedidos.entity.Pedido;
import com.empresa.pedidos.entity.Produto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PedidoMapperTest {

    private final PedidoMapper mapper = Mappers.getMapper(PedidoMapper.class);

    @Test
    void toResponse_deveMapearPedido() {
        Pedido pedido = Pedido.builder()
                .codigoPedido(1L)
                .dataPedido(LocalDateTime.now())
                .codigoCliente(2L)
                .codigoProduto(3L)
                .valorUnitario(BigDecimal.TEN)
                .quantidade(5)
                .status("EM_PROCESSAMENTO")
                .dataCriacao(LocalDateTime.now())
                .build();

        PedidoResponse response = mapper.toResponse(pedido);

        assertThat(response.codigoPedido()).isEqualTo(1L);
        assertThat(response.quantidade()).isEqualTo(5);
    }

    @Test
    void toResponseList_deveMapearLista() {
        Pedido pedido = Pedido.builder().codigoPedido(1L).quantidade(1).status("EM_PROCESSAMENTO").build();
        List<PedidoResponse> responses = mapper.toResponseList(List.of(pedido));
        assertThat(responses).hasSize(1);
    }

    @Test
    void toCacheDto_deveMapearProduto() {
        Produto produto = Produto.builder()
                .codigoProduto(1L)
                .descricao("Produto")
                .valor(BigDecimal.TEN)
                .ativo("S")
                .build();

        ProdutoCacheDto cacheDto = mapper.toCacheDto(produto);

        assertThat(cacheDto.codigoProduto()).isEqualTo(1L);
        assertThat(cacheDto.ativo()).isEqualTo("S");
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
    void toCacheDto_quandoNull_deveRetornarNull() {
        assertThat(mapper.toCacheDto(null)).isNull();
    }
}
