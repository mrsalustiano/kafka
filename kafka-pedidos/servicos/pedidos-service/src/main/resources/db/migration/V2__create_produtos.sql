CREATE TABLE produtos (
    codigo_produto   BIGINT         NOT NULL AUTO_INCREMENT,
    descricao        VARCHAR(255)   NOT NULL,
    valor            DECIMAL(15, 2) NOT NULL,
    ativo            CHAR(1)        NOT NULL DEFAULT 'S',
    data_criacao     TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP      NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_produtos PRIMARY KEY (codigo_produto),
    CONSTRAINT ck_produtos_ativo CHECK (ativo IN ('S', 'N')),
    CONSTRAINT ck_produtos_valor CHECK (valor > 0)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_produtos_ativo ON produtos (ativo);
CREATE INDEX idx_produtos_descricao ON produtos (descricao);
CREATE INDEX idx_produtos_data_criacao ON produtos (data_criacao);
