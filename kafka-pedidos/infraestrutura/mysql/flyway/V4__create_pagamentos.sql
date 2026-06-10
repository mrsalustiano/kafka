CREATE TABLE pagamentos (
    codigo_pedido       BIGINT       NOT NULL,
    tipo_pagamento      VARCHAR(20)  NULL,
    codigo_autorizacao  VARCHAR(100) NULL,
    efetivado           TINYINT(1)   NULL,

    CONSTRAINT pk_pagamentos PRIMARY KEY (codigo_pedido),
    CONSTRAINT fk_pagamentos_pedido FOREIGN KEY (codigo_pedido)
        REFERENCES pedidos (codigo_pedido)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT ck_pagamentos_tipo CHECK (
        tipo_pagamento IS NULL OR tipo_pagamento IN ('CC', 'DEBITO', 'BOLETO', 'PIX')
    ),
    CONSTRAINT ck_pagamentos_efetivado CHECK (efetivado IS NULL OR efetivado IN (0, 1))
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_pagamentos_tipo_pagamento ON pagamentos (tipo_pagamento);
CREATE INDEX idx_pagamentos_efetivado ON pagamentos (efetivado);
