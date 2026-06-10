package com.empresa.broker;

import com.empresa.broker.config.KafkaProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
@EnableConfigurationProperties(KafkaProperties.class)
public class BrokerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BrokerServiceApplication.class, args);
    }
}
