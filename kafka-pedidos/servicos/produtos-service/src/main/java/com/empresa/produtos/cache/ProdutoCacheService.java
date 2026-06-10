package com.empresa.produtos.cache;

import com.empresa.produtos.config.RedisProperties;
import com.empresa.produtos.dto.ProdutoResponse;
import com.empresa.produtos.exception.RedisException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
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
    public Optional<ProdutoResponse> buscarPorId(Long id) {
        try {
            String json = redisTemplate.opsForValue().get(CacheKeyConstants.produtoKey(id));
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, ProdutoResponse.class));
        } catch (Exception ex) {
            throw new RedisException("Erro ao consultar produto no Redis: " + id, ex);
        }
    }

    @Recover
    public Optional<ProdutoResponse> recoverBuscarPorId(RedisException ex, Long id) {
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
    public void salvarProduto(ProdutoResponse produto) {
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
    public void recoverSalvarProduto(RedisException ex, ProdutoResponse produto) {
        log.warn("Fallback Redis ao salvar produto {}: {}", produto.codigoProduto(), ex.getMessage());
    }

    @Retryable(
            retryFor = RedisException.class,
            maxAttemptsExpression = "${app.redis.retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${app.redis.retry.initial-delay-ms:500}",
                    multiplierExpression = "${app.redis.retry.multiplier:2.0}"
            )
    )
    public void salvarTodos(List<ProdutoResponse> produtos) {
        try {
            for (ProdutoResponse produto : produtos) {
                String json = objectMapper.writeValueAsString(produto);
                redisTemplate.opsForValue().set(
                        CacheKeyConstants.produtoKey(produto.codigoProduto()),
                        json,
                        ttlDuration()
                );
            }
            String allJson = objectMapper.writeValueAsString(produtos);
            redisTemplate.opsForValue().set(CacheKeyConstants.PRODUTO_ALL, allJson, ttlDuration());
        } catch (JsonProcessingException ex) {
            throw new RedisException("Erro ao serializar lista de produtos para Redis", ex);
        } catch (Exception ex) {
            throw new RedisException("Erro ao salvar produtos no Redis", ex);
        }
    }

    @Recover
    public void recoverSalvarTodos(RedisException ex, List<ProdutoResponse> produtos) {
        log.warn("Fallback Redis ao salvar lista de produtos: {}", ex.getMessage());
    }

    @Retryable(
            retryFor = RedisException.class,
            maxAttemptsExpression = "${app.redis.retry.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression = "${app.redis.retry.initial-delay-ms:500}",
                    multiplierExpression = "${app.redis.retry.multiplier:2.0}"
            )
    )
    public void removerProduto(Long id) {
        try {
            redisTemplate.delete(CacheKeyConstants.produtoKey(id));
        } catch (Exception ex) {
            throw new RedisException("Erro ao remover produto do Redis: " + id, ex);
        }
    }

    @Recover
    public void recoverRemoverProduto(RedisException ex, Long id) {
        log.warn("Fallback Redis ao remover produto {}: {}", id, ex.getMessage());
    }

    public boolean isDisponivel() {
        try {
            String pong = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .ping();
            return "PONG".equalsIgnoreCase(pong);
        } catch (Exception ex) {
            log.warn("Redis indisponivel: {}", ex.getMessage());
            return false;
        }
    }

    public Optional<List<ProdutoResponse>> buscarTodosCacheados() {
        try {
            String json = redisTemplate.opsForValue().get(CacheKeyConstants.PRODUTO_ALL);
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            List<ProdutoResponse> produtos = objectMapper.readValue(json, new TypeReference<>() {
            });
            return Optional.of(produtos);
        } catch (Exception ex) {
            log.warn("Erro ao buscar produto:all no Redis: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    private Duration ttlDuration() {
        return Duration.ofMinutes(redisProperties.getTtlMinutes());
    }
}
