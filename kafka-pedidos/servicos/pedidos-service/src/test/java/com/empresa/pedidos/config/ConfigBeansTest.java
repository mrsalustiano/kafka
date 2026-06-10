package com.empresa.pedidos.config;

import com.empresa.pedidos.repository.AuditoriaRepository;
import com.empresa.pedidos.repository.ClienteRepository;
import com.empresa.pedidos.repository.MensagemProcessadaRepository;
import com.empresa.pedidos.repository.PedidoRepository;
import com.empresa.pedidos.repository.ProdutoRepository;
import io.lettuce.core.ClientOptions;
import io.swagger.v3.oas.models.OpenAPI;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigBeansTest {

    @Test
    void jdbiConfig_deveCriarJdbi() {
        JdbiConfig config = new JdbiConfig();
        assertThat(config.jdbi(mock(DataSource.class))).isNotNull();
    }

    @Test
    void repositoryConfig_deveCriarRepositorios() {
        RepositoryConfig config = new RepositoryConfig();
        Jdbi jdbi = mock(Jdbi.class);
        when(jdbi.onDemand(PedidoRepository.class)).thenReturn(mock(PedidoRepository.class));
        when(jdbi.onDemand(ClienteRepository.class)).thenReturn(mock(ClienteRepository.class));
        when(jdbi.onDemand(ProdutoRepository.class)).thenReturn(mock(ProdutoRepository.class));
        when(jdbi.onDemand(AuditoriaRepository.class)).thenReturn(mock(AuditoriaRepository.class));
        when(jdbi.onDemand(MensagemProcessadaRepository.class)).thenReturn(mock(MensagemProcessadaRepository.class));

        assertThat(config.pedidoRepository(jdbi)).isNotNull();
        assertThat(config.clienteRepository(jdbi)).isNotNull();
        assertThat(config.produtoRepository(jdbi)).isNotNull();
        assertThat(config.auditoriaRepository(jdbi)).isNotNull();
        assertThat(config.mensagemProcessadaRepository(jdbi)).isNotNull();
    }

    @Test
    void openApiConfig_deveCriarOpenApi() {
        assertThat(new OpenApiConfig().pedidosOpenApi().getInfo().getTitle()).isEqualTo("Pedidos Service API");
    }

    @Test
    void kafkaConfig_deveCriarBeans() {
        KafkaProperties kafkaProperties = new KafkaProperties(
                5000,
                new KafkaProperties.Retry(3, 500L, 2.0),
                new KafkaProperties.Topics("pedido-create", "pedido-create-dlt", "pedido-response", "pedido-response-dlt")
        );
        KafkaConfig config = new KafkaConfig(kafkaProperties);
        ReflectionTestUtils.setField(config, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(config, "groupId", "pedidos-service");
        ReflectionTestUtils.setField(config, "autoOffsetReset", "earliest");
        ReflectionTestUtils.setField(config, "acks", "all");
        ReflectionTestUtils.setField(config, "retries", 3);

        KafkaAdmin admin = config.kafkaAdmin();
        assertThat(admin.getConfigurationProperties()).containsKey("bootstrap.servers");

        var producerFactory = config.producerFactory();
        KafkaTemplate<String, Object> kafkaTemplate = config.kafkaTemplate(producerFactory);
        var consumerFactory = config.consumerFactory();
        var listenerFactory = config.kafkaListenerContainerFactory(consumerFactory, kafkaTemplate);
        DefaultErrorHandler errorHandler = config.kafkaErrorHandler(kafkaTemplate);

        assertThat(kafkaTemplate).isNotNull();
        assertThat(listenerFactory).isNotNull();
        assertThat(errorHandler).isNotNull();
    }

    @Test
    void redisConfig_deveCriarBeans() {
        RedisConfig config = new RedisConfig();
        RedisProperties properties = new RedisProperties();
        properties.setTimeoutMs(1000);
        properties.setTtlMinutes(30);
        properties.getRetry().setMaxAttempts(3);

        LettuceClientConfigurationBuilderCustomizer customizer =
                config.lettuceClientConfigurationBuilderCustomizer(properties);
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder =
                LettuceClientConfiguration.builder();
        customizer.customize(builder);
        ClientOptions clientOptions = builder.build().getClientOptions().orElseThrow();
        assertThat(clientOptions.getSocketOptions().getConnectTimeout().toMillis()).isEqualTo(1000);

        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        RedisTemplate<String, String> template = config.redisTemplate(connectionFactory);
        assertThat(template.getConnectionFactory()).isSameAs(connectionFactory);
        assertThat(config.objectMapper()).isNotNull();
    }

    @Test
    void redisProperties_deveExporValores() {
        RedisProperties properties = new RedisProperties();
        properties.setTtlMinutes(45);
        properties.setTimeoutMs(3000);
        properties.getRetry().setInitialDelayMs(100);
        properties.getRetry().setMultiplier(1.5);

        assertThat(properties.getTtlMinutes()).isEqualTo(45);
        assertThat(properties.getTimeoutMs()).isEqualTo(3000);
        assertThat(properties.getRetry().getInitialDelayMs()).isEqualTo(100);
    }

    @Test
    void kafkaConfig_calcularMaxElapsedTime_deveSomarIntervalos() {
        KafkaProperties kafkaProperties = new KafkaProperties(
                5000,
                new KafkaProperties.Retry(3, 500L, 2.0),
                new KafkaProperties.Topics("pedido-create", "pedido-create-dlt", "pedido-response", "pedido-response-dlt")
        );
        assertThat(new KafkaConfig(kafkaProperties).calcularMaxElapsedTime()).isEqualTo(3500L);
    }

    @Test
    void kafkaErrorHandler_dlqResolver_deveMapearParaPedidoResponseDlt() {
        KafkaProperties kafkaProperties = new KafkaProperties(
                5000,
                new KafkaProperties.Retry(3, 500L, 2.0),
                new KafkaProperties.Topics("pedido-create", "pedido-create-dlt", "pedido-response", "pedido-response-dlt")
        );
        KafkaConfig config = new KafkaConfig(kafkaProperties);
        ReflectionTestUtils.setField(config, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(config, "groupId", "pedidos-service");
        ReflectionTestUtils.setField(config, "autoOffsetReset", "earliest");
        ReflectionTestUtils.setField(config, "acks", "all");
        ReflectionTestUtils.setField(config, "retries", 3);
        KafkaTemplate<String, Object> kafkaTemplate = config.kafkaTemplate(config.producerFactory());
        DefaultErrorHandler errorHandler = config.kafkaErrorHandler(kafkaTemplate);

        ConsumerRecord<String, String> record = new ConsumerRecord<>("pedido-response", 1, 0L, "k", "v");
        TopicPartition partition = extrairTopicoDlt(errorHandler, record);

        assertThat(partition.topic()).isEqualTo("pedido-response-dlt");
        assertThat(partition.partition()).isEqualTo(1);
    }

    @SuppressWarnings("unchecked")
    private TopicPartition extrairTopicoDlt(DefaultErrorHandler errorHandler, ConsumerRecord<String, String> record) {
        Object tracker = ReflectionTestUtils.getField(errorHandler, "failureTracker");
        DeadLetterPublishingRecoverer dltRecoverer =
                (DeadLetterPublishingRecoverer) ReflectionTestUtils.invokeMethod(tracker, "getRecoverer");
        BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition> resolver =
                (BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition>)
                        ReflectionTestUtils.getField(dltRecoverer, "destinationResolver");
        return resolver.apply(record, new RuntimeException("dlq"));
    }
}
