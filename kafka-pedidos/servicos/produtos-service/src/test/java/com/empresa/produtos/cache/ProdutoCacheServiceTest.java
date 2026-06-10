package com.empresa.produtos.cache;

import com.empresa.produtos.config.RedisProperties;
import com.empresa.produtos.dto.ProdutoResponse;
import com.empresa.produtos.exception.RedisException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoCacheServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RedisConnectionFactory connectionFactory;

    @Mock
    private RedisConnection redisConnection;

    private ProdutoCacheService produtoCacheService;
    private ObjectMapper objectMapper;
    private ProdutoResponse produtoResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        RedisProperties redisProperties = new RedisProperties();
        redisProperties.setTtlMinutes(60);
        produtoCacheService = new ProdutoCacheService(redisTemplate, objectMapper, redisProperties);
        produtoResponse = new ProdutoResponse(1L, "Produto", new BigDecimal("10.00"), "S",
                LocalDateTime.now(), null);
    }

    @Test
    void buscarPorId_quandoExiste_deveRetornarProduto() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("produto:1")).thenReturn(objectMapper.writeValueAsString(produtoResponse));

        Optional<ProdutoResponse> result = produtoCacheService.buscarPorId(1L);

        assertThat(result).isPresent();
        assertThat(result.get().descricao()).isEqualTo("Produto");
    }

    @Test
    void buscarPorId_quandoNaoExiste_deveRetornarVazio() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("produto:1")).thenReturn(null);

        Optional<ProdutoResponse> result = produtoCacheService.buscarPorId(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void buscarPorId_quandoErro_deveLancarRedisException() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("produto:1")).thenThrow(new RuntimeException("timeout"));

        assertThatThrownBy(() -> produtoCacheService.buscarPorId(1L))
                .isInstanceOf(RedisException.class);
    }

    @Test
    void recoverBuscarPorId_deveRetornarVazio() {
        Optional<ProdutoResponse> result = produtoCacheService.recoverBuscarPorId(
                new RedisException("erro"), 1L);

        assertThat(result).isEmpty();
    }

    @Test
    void salvarProduto_deveGravarNoRedis() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        produtoCacheService.salvarProduto(produtoResponse);

        verify(valueOperations).set(eq("produto:1"), anyString(), any());
    }

    @Test
    void salvarTodos_deveGravarProdutosELista() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        produtoCacheService.salvarTodos(List.of(produtoResponse));

        verify(valueOperations).set(eq("produto:1"), anyString(), any());
        verify(valueOperations).set(eq("produto:all"), anyString(), any());
    }

    @Test
    void removerProduto_deveExcluirChave() {
        produtoCacheService.removerProduto(1L);

        verify(redisTemplate).delete("produto:1");
    }

    @Test
    void isDisponivel_quandoPong_deveRetornarTrue() {
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenReturn("PONG");

        assertThat(produtoCacheService.isDisponivel()).isTrue();
    }

    @Test
    void isDisponivel_quandoErro_deveRetornarFalse() {
        when(redisTemplate.getConnectionFactory()).thenThrow(new RuntimeException("down"));

        assertThat(produtoCacheService.isDisponivel()).isFalse();
    }

    @Test
    void buscarTodosCacheados_quandoExiste_deveRetornarLista() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("produto:all"))
                .thenReturn(objectMapper.writeValueAsString(List.of(produtoResponse)));

        Optional<List<ProdutoResponse>> result = produtoCacheService.buscarTodosCacheados();

        assertThat(result).isPresent();
        assertThat(result.get()).hasSize(1);
    }

    @Test
    void removerProduto_quandoErro_deveLancarRedisException() {
        doThrow(new RuntimeException("erro")).when(redisTemplate).delete("produto:1");

        assertThatThrownBy(() -> produtoCacheService.removerProduto(1L))
                .isInstanceOf(RedisException.class);
    }

    @Test
    void salvarProduto_quandoErroSerializacao_deveLancarRedisException() throws Exception {
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        when(failingMapper.writeValueAsString(produtoResponse)).thenThrow(new JsonProcessingException("erro") {
        });
        ProdutoCacheService service = new ProdutoCacheService(redisTemplate, failingMapper, new RedisProperties());

        assertThatThrownBy(() -> service.salvarProduto(produtoResponse))
                .isInstanceOf(RedisException.class);
    }

    @Test
    void buscarTodosCacheados_quandoChaveAusente_deveRetornarVazio() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("produto:all")).thenReturn(null);

        Optional<List<ProdutoResponse>> result = produtoCacheService.buscarTodosCacheados();

        assertThat(result).isEmpty();
    }

    @Test
    void buscarTodosCacheados_quandoErro_deveRetornarVazio() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("produto:all")).thenReturn("invalid-json");

        Optional<List<ProdutoResponse>> result = produtoCacheService.buscarTodosCacheados();

        assertThat(result).isEmpty();
    }

    @Test
    void salvarTodos_quandoErroRedis_deveLancarRedisException() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doThrow(new RuntimeException("redis down"))
                .when(valueOperations).set(anyString(), anyString(), any());

        assertThatThrownBy(() -> produtoCacheService.salvarTodos(List.of(produtoResponse)))
                .isInstanceOf(RedisException.class);
    }

    @Test
    void recoverSalvarProduto_naoDevePropagarExcecao() {
        produtoCacheService.recoverSalvarProduto(new RedisException("erro"), produtoResponse);
    }

    @Test
    void recoverSalvarTodos_naoDevePropagarExcecao() {
        produtoCacheService.recoverSalvarTodos(new RedisException("erro"), List.of(produtoResponse));
    }

    @Test
    void recoverRemoverProduto_naoDevePropagarExcecao() {
        produtoCacheService.recoverRemoverProduto(new RedisException("erro"), 1L);
    }
}
