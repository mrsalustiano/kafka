CREATE TABLE pedidos (
    codigo_pedido   BIGINT         NOT NULL AUTO_INCREMENT,
    data_pedido     TIMESTAMP      NOT NULL,
    codigo_cliente  BIGINT         NOT NULL,
    codigo_produto  BIGINT         NOT NULL,
    valor_unitario  DECIMAL(15, 2) NOT NULL,
    quantidade      INT            NOT NULL,
    status          VARCHAR(50)    NOT NULL,
    data_criacao    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_pedidos PRIMARY KEY (codigo_pedido),
    CONSTRAINT fk_pedidos_cliente FOREIGN KEY (codigo_cliente)
        REFERENCES clientes (codigo_cliente)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_pedidos_produto FOREIGN KEY (codigo_produto)
        REFERENCES produtos (codigo_produto)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT ck_pedidos_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_pedidos_valor_unitario CHECK (valor_unitario > 0),
    CONSTRAINT ck_pedidos_status CHECK (status IN (
        'EM_PROCESSAMENTO',
        'FINALIZADO',
        'EM_SEPARACAO',
        'ENTREGUE',
        'CANCELADO',
        'EM_ROTA_DE_ENTREGA'
    ))
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_pedidos_codigo_cliente ON pedidos (codigo_cliente);
CREATE INDEX idx_pedidos_codigo_produto ON pedidos (codigo_produto);
CREATE INDEX idx_pedidos_status ON pedidos (status);
CREATE INDEX idx_pedidos_data_pedido ON pedidos (data_pedido);
CREATE INDEX idx_pedidos_data_criacao ON pedidos (data_criacao);
CREATE INDEX idx_pedidos_cliente_status ON pedidos (codigo_cliente, status);
