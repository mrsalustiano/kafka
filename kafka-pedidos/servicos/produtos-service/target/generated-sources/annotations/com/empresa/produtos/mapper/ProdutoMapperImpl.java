package com.empresa.produtos.mapper;

import com.empresa.produtos.dto.ProdutoRequest;
import com.empresa.produtos.dto.ProdutoResponse;
import com.empresa.produtos.entity.Produto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-09T23:39:05-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Oracle Corporation)"
)
@Component
public class ProdutoMapperImpl implements ProdutoMapper {

    @Override
    public ProdutoResponse toResponse(Produto produto) {
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

        ProdutoResponse produtoResponse = new ProdutoResponse( codigoProduto, descricao, valor, ativo, dataCriacao, dataAtualizacao );

        return produtoResponse;
    }

    @Override
    public List<ProdutoResponse> toResponseList(List<Produto> produtos) {
        if ( produtos == null ) {
            return null;
        }

        List<ProdutoResponse> list = new ArrayList<ProdutoResponse>( produtos.size() );
        for ( Produto produto : produtos ) {
            list.add( toResponse( produto ) );
        }

        return list;
    }

    @Override
    public Produto toEntity(ProdutoRequest request) {
        if ( request == null ) {
            return null;
        }

        Produto.ProdutoBuilder produto = Produto.builder();

        produto.descricao( request.descricao() );
        produto.valor( request.valor() );

        produto.ativo( "S" );

        return produto.build();
    }
}
