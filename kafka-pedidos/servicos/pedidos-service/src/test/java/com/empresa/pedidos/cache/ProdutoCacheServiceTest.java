package com.empresa.pedidos.cache;

import com.empresa.pedidos.config.RedisProperties;
import com.empresa.pedidos.dto.ProdutoCacheDto;
import com.empresa.pedidos.exception.RedisException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    private RedisProperties redisProperties;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private ProdutoCacheService produtoCacheService;

    private ProdutoCacheDto produto;

    @BeforeEach
    void setUp() {
        produtoCacheService = new ProdutoCacheService(redisTemplate, objectMapper, redisProperties);
        produto = new ProdutoCacheDto(1L, "Produto", BigDecimal.TEN, "S", LocalDateTime.now(), null);
    }

    @Test
    void buscarPorId_quandoExiste_deveRetornar() throws Exception {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("produto:1")).thenReturn(objectMapper.writeValueAsString(produto));

        Optional<ProdutoCacheDto> result = produtoCacheService.buscarPorId(1L);

        assertThat(result).isPresent();
        assertThat(result.get().codigoProduto()).isEqualTo(1L);
    }

    @Test
    void buscarPorId_quandoNaoExiste_deveRetornarVazio() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("produto:1")).thenReturn(null);

        assertThat(produtoCacheService.buscarPorId(1L)).isEmpty();
    }

    @Test
    void buscarPorId_quandoErro_deveLancarRedisException() {
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("erro"));

        assertThatThrownBy(() -> produtoCacheService.buscarPorId(1L))
                .isInstanceOf(RedisException.class);
    }

    @Test
    void salvarProduto_devePersistirNoRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisProperties.getTtlMinutes()).thenReturn(60);

        produtoCacheService.salvarProduto(produto);

        verify(valueOperations).set(eq("produto:1"), anyString(), any(java.time.Duration.class));
    }

    @Test
    void salvarProduto_quandoErro_deveLancarRedisException() {
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("erro"));

        assertThatThrownBy(() -> produtoCacheService.salvarProduto(produto))
                .isInstanceOf(RedisException.class);
    }

    @Test
    void recoverBuscarPorId_deveRetornarVazio() {
        Optional<ProdutoCacheDto> result = produtoCacheService.recoverBuscarPorId(
                new RedisException("erro", new RuntimeException()), 1L);

        assertThat(result).isEmpty();
    }

    @Test
    void recoverSalvarProduto_naoDevePropagar() {
        produtoCacheService.recoverSalvarProduto(
                new RedisException("erro", new RuntimeException()), produto);
    }

    @Test
    void buscarPorId_quandoJsonBlank_deveRetornarVazio() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("produto:1")).thenReturn("   ");

        assertThat(produtoCacheService.buscarPorId(1L)).isEmpty();
    }

    @Test
    void salvarProduto_quandoErroSerializacao_deveLancarRedisException() throws Exception {
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        when(failingMapper.writeValueAsString(produto)).thenThrow(new JsonProcessingException("erro") {
        });
        ProdutoCacheService service = new ProdutoCacheService(redisTemplate, failingMapper, redisProperties);

        assertThatThrownBy(() -> service.salvarProduto(produto))
                .isInstanceOf(RedisException.class);
    }
}
