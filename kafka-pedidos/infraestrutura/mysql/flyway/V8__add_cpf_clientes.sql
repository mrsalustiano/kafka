ALTER TABLE clientes
    ADD COLUMN cpf VARCHAR(11) NOT NULL AFTER nome;

ALTER TABLE clientes
    ADD CONSTRAINT uk_clientes_cpf UNIQUE (cpf);

CREATE INDEX idx_clientes_cpf ON clientes (cpf);
