CREATE TABLE clientes (
    codigo_cliente   BIGINT       NOT NULL AUTO_INCREMENT,
    nome             VARCHAR(200) NOT NULL,
    endereco         VARCHAR(255) NULL,
    cep              VARCHAR(20)  NULL,
    cidade           VARCHAR(100) NULL,
    estado           CHAR(2)      NULL,
    email            VARCHAR(150) NULL,
    telefone         VARCHAR(30)  NULL,
    ativo            CHAR(1)      NOT NULL DEFAULT 'S',
    data_criacao     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP    NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_clientes PRIMARY KEY (codigo_cliente),
    CONSTRAINT ck_clientes_ativo CHECK (ativo IN ('S', 'N')),
    CONSTRAINT ck_clientes_estado CHECK (estado IS NULL OR CHAR_LENGTH(estado) = 2)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_clientes_ativo ON clientes (ativo);
CREATE INDEX idx_clientes_email ON clientes (email);
CREATE INDEX idx_clientes_nome ON clientes (nome);
CREATE INDEX idx_clientes_data_criacao ON clientes (data_criacao);
