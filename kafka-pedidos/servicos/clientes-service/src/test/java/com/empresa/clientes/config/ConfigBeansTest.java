package com.empresa.clientes.config;

import com.empresa.clientes.repository.AuditoriaRepository;
import com.empresa.clientes.repository.ClienteRepository;
import com.empresa.clientes.repository.ClienteStatusRepository;
import com.empresa.clientes.repository.MensagemProcessadaRepository;
import io.swagger.v3.oas.models.OpenAPI;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigBeansTest {

    @Test
    void jdbiConfig_deveCriarJdbi() {
        JdbiConfig config = new JdbiConfig();
        Jdbi jdbi = config.jdbi(mock(DataSource.class));
        assertThat(jdbi).isNotNull();
    }

    @Test
    void repositoryConfig_deveCriarRepositorios() {
        RepositoryConfig config = new RepositoryConfig();
        Jdbi jdbi = mock(Jdbi.class);
        ClienteRepository clienteRepository = mock(ClienteRepository.class);
        ClienteStatusRepository clienteStatusRepository = mock(ClienteStatusRepository.class);
        AuditoriaRepository auditoriaRepository = mock(AuditoriaRepository.class);
        MensagemProcessadaRepository mensagemProcessadaRepository = mock(MensagemProcessadaRepository.class);

        when(jdbi.onDemand(ClienteRepository.class)).thenReturn(clienteRepository);
        when(jdbi.onDemand(ClienteStatusRepository.class)).thenReturn(clienteStatusRepository);
        when(jdbi.onDemand(AuditoriaRepository.class)).thenReturn(auditoriaRepository);
        when(jdbi.onDemand(MensagemProcessadaRepository.class)).thenReturn(mensagemProcessadaRepository);

        assertThat(config.clienteRepository(jdbi)).isSameAs(clienteRepository);
        assertThat(config.clienteStatusRepository(jdbi)).isSameAs(clienteStatusRepository);
        assertThat(config.auditoriaRepository(jdbi)).isSameAs(auditoriaRepository);
        assertThat(config.mensagemProcessadaRepository(jdbi)).isSameAs(mensagemProcessadaRepository);
    }

    @Test
    void openApiConfig_deveCriarOpenApi() {
        OpenApiConfig config = new OpenApiConfig();
        OpenAPI openAPI = config.clientesOpenApi();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Clientes Service API");
    }

    @Test
    void kafkaConfig_deveCriarBeans() {
        KafkaProperties kafkaProperties = new KafkaProperties(
                5000,
                new KafkaProperties.Retry(3, 500L, 2.0),
                new KafkaProperties.Topics("client-create", "client-create-dlt", "client-response", "client-response-dlt")
        );
        KafkaConfig config = new KafkaConfig(kafkaProperties);
        ReflectionTestUtils.setField(config, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(config, "groupId", "clientes-service");
        ReflectionTestUtils.setField(config, "autoOffsetReset", "earliest");
        ReflectionTestUtils.setField(config, "acks", "all");
        ReflectionTestUtils.setField(config, "retries", 3);

        KafkaAdmin admin = config.kafkaAdmin();
        assertThat(admin.getConfigurationProperties()).containsKey("bootstrap.servers");

        var producerFactory = config.producerFactory();
        assertThat(producerFactory).isNotNull();

        KafkaTemplate<String, Object> kafkaTemplate = config.kafkaTemplate(producerFactory);
        assertThat(kafkaTemplate).isNotNull();

        var consumerFactory = config.consumerFactory();
        assertThat(consumerFactory).isNotNull();

        var listenerFactory = config.kafkaListenerContainerFactory(consumerFactory, kafkaTemplate);
        assertThat(listenerFactory).isNotNull();

        DefaultErrorHandler errorHandler = config.kafkaErrorHandler(kafkaTemplate);
        assertThat(errorHandler).isNotNull();
    }
}
