-- Banco compartilhado pelos microsserviços (skill kafka-pedidos)
CREATE DATABASE IF NOT EXISTS kafka_pedidos
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'kafka_user'@'%' IDENTIFIED BY 'kafka_pass';

GRANT ALL PRIVILEGES ON kafka_pedidos.* TO 'kafka_user'@'%';

FLUSH PRIVILEGES;
