package com.empresa.pedidos.exception;

import com.empresa.pedidos.util.CorrelationIdUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        CorrelationIdUtil.set("corr-123");
        when(request.getRequestURI()).thenReturn("/api/v1/pedidos/1");
    }

    @AfterEach
    void tearDown() {
        CorrelationIdUtil.clear();
    }

    @Test
    void handleNotFound() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(new NotFoundException("nao encontrado"), request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().correlationId()).isEqualTo("corr-123");
    }

    @Test
    void handleValidation() {
        assertThat(handler.handleValidation(new ValidationException("invalido"), request).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleBusiness() {
        assertThat(handler.handleBusiness(new BusinessException("regra"), request).getStatusCode())
                .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void handleMethodArgumentNotValid() {
        BindingResult bindingResult = org.mockito.Mockito.mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("obj", "quantidade", "obrigatorio")));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValid(ex, request);
        assertThat(response.getBody().message()).contains("quantidade");
    }

    @Test
    void handleDatabase() {
        assertThat(handler.handleDatabase(new DatabaseException("erro", new RuntimeException()), request).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void handleRedis() {
        assertThat(handler.handleRedis(new RedisException("redis", new RuntimeException()), request).getStatusCode())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void handleKafkaPublish() {
        assertThat(handler.handleKafkaPublish(new KafkaPublishException("kafka", new RuntimeException()), request).getStatusCode())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void handleKafkaConsume() {
        assertThat(handler.handleKafkaConsume(new KafkaConsumeException("kafka", new RuntimeException()), request).getStatusCode())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void handleTimeout() {
        assertThat(handler.handleTimeout(new TimeoutException("timeout", new RuntimeException()), request).getStatusCode())
                .isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
    }

    @Test
    void handleGeneric() {
        assertThat(handler.handleGeneric(new RuntimeException("x"), request).getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
