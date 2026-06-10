package com.empresa.produtos.config;

import com.empresa.produtos.repository.AuditoriaRepository;
import com.empresa.produtos.repository.ProdutoRepository;
import io.swagger.v3.oas.models.OpenAPI;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import io.lettuce.core.ClientOptions;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaAdmin;
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
        ProdutoRepository produtoRepository = mock(ProdutoRepository.class);
        AuditoriaRepository auditoriaRepository = mock(AuditoriaRepository.class);
        when(jdbi.onDemand(ProdutoRepository.class)).thenReturn(produtoRepository);
        when(jdbi.onDemand(AuditoriaRepository.class)).thenReturn(auditoriaRepository);

        assertThat(config.produtoRepository(jdbi)).isSameAs(produtoRepository);
        assertThat(config.auditoriaRepository(jdbi)).isSameAs(auditoriaRepository);
    }

    @Test
    void openApiConfig_deveCriarOpenApi() {
        OpenApiConfig config = new OpenApiConfig();
        OpenAPI openAPI = config.produtosOpenApi();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Produtos Service API");
    }

    @Test
    void kafkaConfig_deveCriarKafkaAdmin() {
        KafkaConfig config = new KafkaConfig();
        ReflectionTestUtils.setField(config, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(config, "timeoutMs", 5000);
        KafkaAdmin admin = config.kafkaAdmin();
        assertThat(admin.getConfigurationProperties()).containsKey("bootstrap.servers");
    }

    @Test
    void redisConfig_deveCriarBeans() {
        RedisConfig config = new RedisConfig();
        RedisProperties properties = new RedisProperties();
        properties.setTimeoutMs(1000);

        LettuceClientConfigurationBuilderCustomizer customizer =
                config.lettuceClientConfigurationBuilderCustomizer(properties);
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder =
                LettuceClientConfiguration.builder();
        customizer.customize(builder);
        LettuceClientConfiguration clientConfiguration = builder.build();
        ClientOptions clientOptions = clientConfiguration.getClientOptions().orElseThrow();
        assertThat(clientOptions.getSocketOptions().getConnectTimeout().toMillis()).isEqualTo(1000);

        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        RedisTemplate<String, String> template = config.redisTemplate(connectionFactory);
        assertThat(template.getConnectionFactory()).isSameAs(connectionFactory);
        assertThat(config.objectMapper()).isNotNull();
    }
}
