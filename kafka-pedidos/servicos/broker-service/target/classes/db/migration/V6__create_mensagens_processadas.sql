CREATE TABLE mensagens_processadas (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    event_id            VARCHAR(255) NOT NULL,
    data_processamento  TIMESTAMP    NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_mensagens_processadas PRIMARY KEY (id),
    CONSTRAINT uk_mensagens_processadas_event_id UNIQUE (event_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_mensagens_processadas_data ON mensagens_processadas (data_processamento);
