package com.empresa.pedidos.mapper;

import com.empresa.pedidos.dto.PedidoResponse;
import com.empresa.pedidos.dto.ProdutoCacheDto;
import com.empresa.pedidos.entity.Pedido;
import com.empresa.pedidos.entity.Produto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PedidoMapper {

    PedidoResponse toResponse(Pedido pedido);

    List<PedidoResponse> toResponseList(List<Pedido> pedidos);

    ProdutoCacheDto toCacheDto(Produto produto);
}
