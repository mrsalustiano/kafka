package com.empresa.pedidos.repository;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface MensagemProcessadaRepository {

    @SqlQuery("SELECT COUNT(*) > 0 FROM mensagens_processadas WHERE event_id = :eventId")
    boolean existsByEventId(@Bind("eventId") String eventId);

    @SqlUpdate("INSERT INTO mensagens_processadas (event_id) VALUES (:eventId)")
    void insert(@Bind("eventId") String eventId);
}
