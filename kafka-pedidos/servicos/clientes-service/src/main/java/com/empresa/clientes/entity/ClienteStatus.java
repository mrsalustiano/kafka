package com.empresa.clientes.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = @JdbiConstructor)
public class ClienteStatus {

    @ColumnName("codigo_cliente")
    private Long codigoCliente;

    private String status;

    @ColumnName("data_atualizacao")
    private LocalDateTime dataAtualizacao;
}
