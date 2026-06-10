package com.empresa.pedidos.config;

import com.empresa.pedidos.util.CorrelationIdUtil;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CorrelationIdFilterTest {

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private CorrelationIdFilter correlationIdFilter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(correlationIdFilter, "correlationHeader", "X-Correlation-Id");
    }

    @AfterEach
    void tearDown() {
        CorrelationIdUtil.clear();
        MDC.clear();
    }

    @Test
    void doFilterInternal_comHeaderExistente_devePropagar() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "existente");
        MockHttpServletResponse response = new MockHttpServletResponse();

        correlationIdFilter.doFilterInternal(request, response, filterChain);

        assertThat(CorrelationIdUtil.get()).isNull();
        assertThat(response.getHeader("X-Correlation-Id")).isEqualTo("existente");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_semHeader_deveGerarUuid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        correlationIdFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getHeader("X-Correlation-Id")).isNotBlank();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_comHeaderBlank_deveGerarUuid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "   ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        correlationIdFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getHeader("X-Correlation-Id")).isNotBlank();
    }

    @Test
    void doFilterInternal_duranteRequisicao_devePopularMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "corr-mdc");
        MockHttpServletResponse response = new MockHttpServletResponse();

        doAnswer(invocation -> {
            assertThat(CorrelationIdUtil.get()).isEqualTo("corr-mdc");
            assertThat(MDC.get("correlationId")).isEqualTo("corr-mdc");
            return null;
        }).when(filterChain).doFilter(request, response);

        correlationIdFilter.doFilterInternal(request, response, filterChain);
    }
}
