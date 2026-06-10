package com.empresa.produtos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.empresa.produtos.config.CacheProperties;
import com.empresa.produtos.config.RedisProperties;

@SpringBootApplication
@EnableScheduling
@EnableRetry
@EnableConfigurationProperties({CacheProperties.class, RedisProperties.class})
public class ProdutosServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProdutosServiceApplication.class, args);
    }
}
