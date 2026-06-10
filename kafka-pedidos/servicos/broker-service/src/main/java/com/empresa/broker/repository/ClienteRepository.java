package com.empresa.broker.repository;

import com.empresa.broker.entity.Cliente;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterConstructorMapper(Cliente.class)
public interface ClienteRepository {

    @SqlUpdate("""
            INSERT INTO clientes (nome, endereco, cep, cidade, estado, email, telefone, ativo)
            VALUES (:nome, :endereco, :cep, :cidade, :estado, :email, :telefone, :ativo)
            """)
    @GetGeneratedKeys("codigo_cliente")
    long insert(@BindBean Cliente cliente);
}
