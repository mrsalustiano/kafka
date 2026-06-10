package com.empresa.produtos.mapper;

import com.empresa.produtos.dto.ProdutoRequest;
import com.empresa.produtos.dto.ProdutoResponse;
import com.empresa.produtos.entity.Produto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProdutoMapper {

    ProdutoResponse toResponse(Produto produto);

    List<ProdutoResponse> toResponseList(List<Produto> produtos);

    @Mapping(target = "codigoProduto", ignore = true)
    @Mapping(target = "ativo", constant = "S")
    @Mapping(target = "dataCriacao", ignore = true)
    @Mapping(target = "dataAtualizacao", ignore = true)
    Produto toEntity(ProdutoRequest request);
}
