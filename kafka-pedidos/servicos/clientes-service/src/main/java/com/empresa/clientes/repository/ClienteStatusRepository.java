package com.empresa.clientes.repository;

import com.empresa.clientes.entity.ClienteStatus;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;

@RegisterConstructorMapper(ClienteStatus.class)
public interface ClienteStatusRepository {

    @SqlUpdate("""
            INSERT INTO cliente_status (codigo_cliente, status)
            VALUES (:codigoCliente, :status)
            ON DUPLICATE KEY UPDATE
                status = :status,
                data_atualizacao = CURRENT_TIMESTAMP
            """)
    void upsert(@Bind("codigoCliente") Long codigoCliente, @Bind("status") String status);

    @SqlQuery("""
            SELECT codigo_cliente, status, data_atualizacao
            FROM cliente_status
            WHERE codigo_cliente = :id
            """)
    Optional<ClienteStatus> findById(@Bind("id") Long id);
}
