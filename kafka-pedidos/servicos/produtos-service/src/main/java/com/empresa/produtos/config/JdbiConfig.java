package com.empresa.produtos.config;

import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.jdbi.v3.core.statement.SqlStatements;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.empresa.produtos.entity.Produto;

import javax.sql.DataSource;

@Configuration
public class JdbiConfig {

    @Bean
    public Jdbi jdbi(DataSource dataSource) {
        Jdbi jdbi = Jdbi.create(dataSource);
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.registerRowMapper(ConstructorMapper.factory(Produto.class));
        jdbi.getConfig(SqlStatements.class).setUnusedBindingAllowed(true);
        return jdbi;
    }
}
