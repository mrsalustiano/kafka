package com.empresa.produtos.repository;

import com.empresa.produtos.entity.Produto;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

@RegisterConstructorMapper(Produto.class)
public interface ProdutoRepository {

    @SqlUpdate("""
            INSERT INTO produtos (descricao, valor, ativo)
            VALUES (:descricao, :valor, :ativo)
            """)
    @GetGeneratedKeys("codigo_produto")
    long insert(@BindBean Produto produto);

    @SqlQuery("SELECT * FROM produtos WHERE codigo_produto = :id AND ativo = 'S'")
    Optional<Produto> findAtivoById(@Bind("id") Long id);

    @SqlQuery("SELECT * FROM produtos WHERE codigo_produto = :id")
    Optional<Produto> findById(@Bind("id") Long id);

    @SqlQuery("SELECT * FROM produtos WHERE ativo = 'S' ORDER BY codigo_produto")
    List<Produto> findAllAtivos();

    @SqlQuery("""
            SELECT * FROM produtos
            WHERE ativo = 'S'
            ORDER BY <sortColumn> <sortDirection>
            LIMIT :limit OFFSET :offset
            """)
    List<Produto> findAllAtivosPaginado(@Define("sortColumn") String sortColumn,
                                        @Define("sortDirection") String sortDirection,
                                        @Bind("limit") int limit,
                                        @Bind("offset") long offset);

    @SqlQuery("SELECT COUNT(*) FROM produtos WHERE ativo = 'S'")
    long countAtivos();

    @SqlUpdate("""
            UPDATE produtos
            SET descricao = :descricao, valor = :valor
            WHERE codigo_produto = :codigoProduto AND ativo = 'S'
            """)
    int update(@BindBean Produto produto);

    @SqlUpdate("UPDATE produtos SET ativo = 'N' WHERE codigo_produto = :id AND ativo = 'S'")
    int desativar(@Bind("id") Long id);
}
