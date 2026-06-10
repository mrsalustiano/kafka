package com.empresa.pedidos.repository;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface AuditoriaRepository {

    @SqlUpdate("""
            INSERT INTO auditoria (data_evento, operacao, entidade, id_entidade, correlation_id, detalhes)
            VALUES (NOW(), :operacao, :entidade, :idEntidade, :correlationId, :detalhes)
            """)
    void registrar(@Bind("operacao") String operacao,
                   @Bind("entidade") String entidade,
                   @Bind("idEntidade") Long idEntidade,
                   @Bind("correlationId") String correlationId,
                   @Bind("detalhes") String detalhes);
}
