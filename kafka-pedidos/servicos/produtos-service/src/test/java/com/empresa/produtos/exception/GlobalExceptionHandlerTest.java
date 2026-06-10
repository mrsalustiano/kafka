package com.empresa.produtos.exception;

import com.empresa.produtos.util.CorrelationIdUtil;
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
        when(request.getRequestURI()).thenReturn("/api/v1/produtos/1");
    }

    @AfterEach
    void tearDown() {
        CorrelationIdUtil.clear();
    }

    @Test
    void handleNotFound() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(
                new NotFoundException("nao encontrado"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().correlationId()).isEqualTo("corr-123");
    }

    @Test
    void handleValidation() {
        ResponseEntity<ErrorResponse> response = handler.handleValidation(
                new ValidationException("invalido"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void handleBusiness() {
        ResponseEntity<ErrorResponse> response = handler.handleBusiness(
                new BusinessException("regra"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    void handleMethodArgumentNotValid() {
        BindingResult bindingResult = org.mockito.Mockito.mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("obj", "descricao", "obrigatorio")));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValid(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).contains("descricao");
    }

    @Test
    void handleDatabase() {
        ResponseEntity<ErrorResponse> response = handler.handleDatabase(
                new DatabaseException("erro", new RuntimeException()), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void handleRedis() {
        ResponseEntity<ErrorResponse> response = handler.handleRedis(
                new RedisException("redis down"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void handleKafkaPublish() {
        ResponseEntity<ErrorResponse> response = handler.handleKafkaPublish(
                new KafkaPublishException("kafka", new RuntimeException()), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void handleKafkaConsume() {
        ResponseEntity<ErrorResponse> response = handler.handleKafkaConsume(
                new KafkaConsumeException("kafka", new RuntimeException()), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void handleTimeout() {
        ResponseEntity<ErrorResponse> response = handler.handleTimeout(
                new TimeoutException("timeout"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
    }

    @Test
    void handleGeneric() {
        ResponseEntity<ErrorResponse> response = handler.handleGeneric(new RuntimeException("x"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
