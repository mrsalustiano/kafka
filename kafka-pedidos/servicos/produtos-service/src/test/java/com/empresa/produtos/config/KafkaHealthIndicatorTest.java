package com.empresa.produtos.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.common.KafkaFuture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaHealthIndicatorTest {

    @Mock
    private KafkaAdmin kafkaAdmin;

    @InjectMocks
    private KafkaHealthIndicator kafkaHealthIndicator;

    @Test
    void health_quandoKafkaDisponivel_deveRetornarUp() throws Exception {
        Map<String, Object> configs = new HashMap<>();
        when(kafkaAdmin.getConfigurationProperties()).thenReturn(configs);

        AdminClient adminClient = mock(AdminClient.class);
        DescribeClusterResult describeClusterResult = mock(DescribeClusterResult.class);
        KafkaFuture<String> clusterIdFuture = mock(KafkaFuture.class);

        when(adminClient.describeCluster()).thenReturn(describeClusterResult);
        when(describeClusterResult.clusterId()).thenReturn(clusterIdFuture);
        when(clusterIdFuture.get(anyLong(), any(TimeUnit.class))).thenReturn("cluster-1");

        try (MockedStatic<AdminClient> adminClientStatic = mockStatic(AdminClient.class)) {
            adminClientStatic.when(() -> AdminClient.create(configs)).thenReturn(adminClient);

            Health health = kafkaHealthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.UP);
        }
    }

    @Test
    void health_quandoKafkaIndisponivel_deveRetornarDown() {
        Map<String, Object> configs = new HashMap<>();
        when(kafkaAdmin.getConfigurationProperties()).thenReturn(configs);

        try (MockedStatic<AdminClient> adminClientStatic = mockStatic(AdminClient.class)) {
            adminClientStatic.when(() -> AdminClient.create(configs)).thenThrow(new RuntimeException("down"));

            Health health = kafkaHealthIndicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        }
    }
}
