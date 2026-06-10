package com.empresa.pedidos.repository;

import com.empresa.pedidos.entity.Pedido;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

@RegisterConstructorMapper(Pedido.class)
public interface PedidoRepository {

    @SqlUpdate("""
            INSERT INTO pedidos (data_pedido, codigo_cliente, codigo_produto, valor_unitario, quantidade, status)
            VALUES (:dataPedido, :codigoCliente, :codigoProduto, :valorUnitario, :quantidade, :status)
            """)
    @GetGeneratedKeys("codigo_pedido")
    long insert(@BindBean Pedido pedido);

    @SqlQuery("SELECT * FROM pedidos WHERE codigo_pedido = :id")
    Optional<Pedido> findById(@Bind("id") Long id);

    @SqlQuery("""
            SELECT * FROM pedidos
            ORDER BY <sortColumn> <sortDirection>
            LIMIT :limit OFFSET :offset
            """)
    List<Pedido> findAllPaginado(@Define("sortColumn") String sortColumn,
                                 @Define("sortDirection") String sortDirection,
                                 @Bind("limit") int limit,
                                 @Bind("offset") long offset);

    @SqlQuery("SELECT COUNT(*) FROM pedidos")
    long count();

    @SqlUpdate("""
            UPDATE pedidos
            SET codigo_cliente = :codigoCliente,
                codigo_produto = :codigoProduto,
                quantidade = :quantidade
            WHERE codigo_pedido = :codigoPedido
            """)
    int update(@BindBean Pedido pedido);

    @SqlUpdate("UPDATE pedidos SET status = :status WHERE codigo_pedido = :id")
    int atualizarStatus(@Bind("id") Long id, @Bind("status") String status);
}
