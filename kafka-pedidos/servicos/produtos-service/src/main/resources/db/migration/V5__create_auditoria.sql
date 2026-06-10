CREATE TABLE auditoria (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    data_evento    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    operacao       VARCHAR(50)  NULL,
    entidade       VARCHAR(100) NULL,
    id_entidade    BIGINT       NULL,
    correlation_id VARCHAR(100) NULL,
    detalhes       TEXT         NULL,

    CONSTRAINT pk_auditoria PRIMARY KEY (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_auditoria_data_evento ON auditoria (data_evento);
CREATE INDEX idx_auditoria_operacao ON auditoria (operacao);
CREATE INDEX idx_auditoria_entidade ON auditoria (entidade);
CREATE INDEX idx_auditoria_id_entidade ON auditoria (id_entidade);
CREATE INDEX idx_auditoria_correlation_id ON auditoria (correlation_id);
CREATE INDEX idx_auditoria_entidade_id ON auditoria (entidade, id_entidade);
