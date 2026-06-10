CREATE TABLE cliente_status (
    codigo_cliente   BIGINT       NOT NULL,
    status           VARCHAR(50)  NOT NULL,
    data_atualizacao TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_cliente_status PRIMARY KEY (codigo_cliente),
    CONSTRAINT fk_cliente_status_cliente FOREIGN KEY (codigo_cliente)
        REFERENCES clientes (codigo_cliente)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_cliente_status_status ON cliente_status (status);
