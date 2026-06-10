package com.empresa.pedidos.mapper;

import com.empresa.pedidos.dto.PedidoResponse;
import com.empresa.pedidos.dto.ProdutoCacheDto;
import com.empresa.pedidos.entity.Pedido;
import com.empresa.pedidos.entity.Produto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-10T01:48:25-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Oracle Corporation)"
)
@Component
public class PedidoMapperImpl implements PedidoMapper {

    @Override
    public PedidoResponse toResponse(Pedido pedido) {
        if ( pedido == null ) {
            return null;
        }

        Long codigoPedido = null;
        LocalDateTime dataPedido = null;
        Long codigoCliente = null;
        Long codigoProduto = null;
        BigDecimal valorUnitario = null;
        Integer quantidade = null;
        String status = null;
        LocalDateTime dataCriacao = null;

        codigoPedido = pedido.getCodigoPedido();
        dataPedido = pedido.getDataPedido();
        codigoCliente = pedido.getCodigoCliente();
        codigoProduto = pedido.getCodigoProduto();
        valorUnitario = pedido.getValorUnitario();
        quantidade = pedido.getQuantidade();
        status = pedido.getStatus();
        dataCriacao = pedido.getDataCriacao();

        PedidoResponse pedidoResponse = new PedidoResponse( codigoPedido, dataPedido, codigoCliente, codigoProduto, valorUnitario, quantidade, status, dataCriacao );

        return pedidoResponse;
    }

    @Override
    public List<PedidoResponse> toResponseList(List<Pedido> pedidos) {
        if ( pedidos == null ) {
            return null;
        }

        List<PedidoResponse> list = new ArrayList<PedidoResponse>( pedidos.size() );
        for ( Pedido pedido : pedidos ) {
            list.add( toResponse( pedido ) );
        }

        return list;
    }

    @Override
    public ProdutoCacheDto toCacheDto(Produto produto) {
        if ( produto == null ) {
            return null;
        }

        Long codigoProduto = null;
        String descricao = null;
        BigDecimal valor = null;
        String ativo = null;
        LocalDateTime dataCriacao = null;
        LocalDateTime dataAtualizacao = null;

        codigoProduto = produto.getCodigoProduto();
        descricao = produto.getDescricao();
        valor = produto.getValor();
        ativo = produto.getAtivo();
        dataCriacao = produto.getDataCriacao();
        dataAtualizacao = produto.getDataAtualizacao();

        ProdutoCacheDto produtoCacheDto = new ProdutoCacheDto( codigoProduto, descricao, valor, ativo, dataCriacao, dataAtualizacao );

        return produtoCacheDto;
    }
}
