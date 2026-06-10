package com.empresa.clientes.repository;

import com.empresa.clientes.entity.Cliente;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Define;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

@RegisterConstructorMapper(Cliente.class)
public interface ClienteRepository {

    @SqlUpdate("""
            INSERT INTO clientes (nome, cpf, endereco, cep, cidade, estado, email, telefone, ativo)
            VALUES (:nome, :cpf, :endereco, :cep, :cidade, :estado, :email, :telefone, :ativo)
            """)
    @GetGeneratedKeys("codigo_cliente")
    long insert(@BindBean Cliente cliente);

    @SqlQuery("SELECT * FROM clientes WHERE codigo_cliente = :id AND ativo = 'S'")
    Optional<Cliente> findAtivoById(@Bind("id") Long id);

    @SqlQuery("SELECT * FROM clientes WHERE codigo_cliente = :id")
    Optional<Cliente> findById(@Bind("id") Long id);

    @SqlQuery("SELECT * FROM clientes WHERE cpf = :cpf AND ativo = 'S'")
    Optional<Cliente> findAtivoByCpf(@Bind("cpf") String cpf);

    @SqlQuery("SELECT COUNT(*) > 0 FROM clientes WHERE cpf = :cpf AND ativo = 'S'")
    boolean existsAtivoByCpf(@Bind("cpf") String cpf);

    @SqlQuery("""
            SELECT COUNT(*) > 0 FROM clientes
            WHERE cpf = :cpf AND ativo = 'S' AND codigo_cliente <> :id
            """)
    boolean existsAtivoByCpfAndNotId(@Bind("cpf") String cpf, @Bind("id") Long id);

    @SqlQuery("""
            SELECT * FROM clientes
            WHERE ativo = 'S'
            ORDER BY <sortColumn> <sortDirection>
            LIMIT :limit OFFSET :offset
            """)
    List<Cliente> findAllAtivosPaginado(@Define("sortColumn") String sortColumn,
                                        @Define("sortDirection") String sortDirection,
                                        @Bind("limit") int limit,
                                        @Bind("offset") long offset);

    @SqlQuery("SELECT COUNT(*) FROM clientes WHERE ativo = 'S'")
    long countAtivos();

    @SqlUpdate("""
            UPDATE clientes
            SET nome = :nome,
                cpf = :cpf,
                endereco = :endereco,
                cep = :cep,
                cidade = :cidade,
                estado = :estado,
                email = :email,
                telefone = :telefone
            WHERE codigo_cliente = :codigoCliente AND ativo = 'S'
            """)
    int update(@BindBean Cliente cliente);

    @SqlUpdate("UPDATE clientes SET ativo = 'N' WHERE codigo_cliente = :id AND ativo = 'S'")
    int desativar(@Bind("id") Long id);
}
