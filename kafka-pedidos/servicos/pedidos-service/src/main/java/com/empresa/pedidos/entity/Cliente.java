package com.empresa.pedidos.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = @JdbiConstructor)
public class Cliente {

    @ColumnName("codigo_cliente")
    private Long codigoCliente;

    private String nome;

    private String ativo;

    @ColumnName("data_criacao")
    private LocalDateTime dataCriacao;
}
