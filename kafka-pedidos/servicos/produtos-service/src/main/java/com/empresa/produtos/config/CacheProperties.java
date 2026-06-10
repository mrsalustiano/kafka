package com.empresa.produtos.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.cache.produtos")
@Getter
@Setter
public class CacheProperties {

    private int refreshMinutes = 30;
}
