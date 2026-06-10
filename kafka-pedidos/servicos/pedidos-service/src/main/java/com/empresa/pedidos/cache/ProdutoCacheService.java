package com.empresa.pedidos.cache;

import com.empresa.pedidos.config.RedisProperties;
import com.empresa.pedidos.dto.ProdutoCacheDto;
import com.empresa.pedidos.exception.RedisException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProdutoCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final RedisProperties redisProperties;

    @Retryable(
            retryFor = RedisException.class,
            maxAttemptsExpression = "${app.redis.retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${app.redis.retry.initial-delay-ms:500}",
                    multiplierExpression = "${app.redis.retry.multiplier:2.0}"
            )
    )
    public Optional<ProdutoCacheDto> buscarPorId(Long id) {
        try {
            String json = redisTemplate.opsForValue().get(CacheKeyConstants.produtoKey(id));
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, ProdutoCacheDto.class));
        } catch (Exception ex) {
            throw new RedisException("Erro ao consultar produto no Redis: " + id, ex);
        }
    }

    @Recover
    public Optional<ProdutoCacheDto> recoverBuscarPorId(RedisException ex, Long id) {
        log.warn("Fallback Redis ao buscar produto {}: {}", id, ex.getMessage());
        return Optional.empty();
    }

    @Retryable(
            retryFor = RedisException.class,
            maxAttemptsExpression = "${app.redis.retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${app.redis.retry.initial-delay-ms:500}",
                    multiplierExpression = "${app.redis.retry.multiplier:2.0}"
            )
    )
    public void salvarProduto(ProdutoCacheDto produto) {
        try {
            String json = objectMapper.writeValueAsString(produto);
            redisTemplate.opsForValue().set(
                    CacheKeyConstants.produtoKey(produto.codigoProduto()),
                    json,
                    ttlDuration()
            );
        } catch (JsonProcessingException ex) {
            throw new RedisException("Erro ao serializar produto para Redis", ex);
        } catch (Exception ex) {
            throw new RedisException("Erro ao salvar produto no Redis", ex);
        }
    }

    @Recover
    public void recoverSalvarProduto(RedisException ex, ProdutoCacheDto produto) {
        log.warn("Fallback Redis ao salvar produto {}: {}", produto.codigoProduto(), ex.getMessage());
    }

    private Duration ttlDuration() {
        return Duration.ofMinutes(redisProperties.getTtlMinutes());
    }
}
