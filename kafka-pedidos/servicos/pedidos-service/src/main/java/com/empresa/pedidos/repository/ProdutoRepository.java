package com.empresa.pedidos.repository;

import com.empresa.pedidos.entity.Produto;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.Optional;

@RegisterConstructorMapper(Produto.class)
public interface ProdutoRepository {

    @SqlQuery("SELECT * FROM produtos WHERE codigo_produto = :id AND ativo = 'S'")
    Optional<Produto> findAtivoById(@Bind("id") Long id);
}
