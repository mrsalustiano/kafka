package com.empresa.pedidos.repository;

import com.empresa.pedidos.entity.Cliente;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.Optional;

@RegisterConstructorMapper(Cliente.class)
public interface ClienteRepository {

    @SqlQuery("SELECT * FROM clientes WHERE codigo_cliente = :id AND ativo = 'S'")
    Optional<Cliente> findAtivoById(@Bind("id") Long id);
}
