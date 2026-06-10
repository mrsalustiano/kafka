package com.empresa.clientes.config;

import com.empresa.clientes.util.CorrelationIdUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    @Value("${app.correlation-id.header}")
    private String correlationHeader;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String correlationId = CorrelationIdUtil.resolveOrGenerate(request.getHeader(correlationHeader));
            CorrelationIdUtil.set(correlationId);
            MDC.put("correlationId", correlationId);
            response.setHeader(correlationHeader, correlationId);
            filterChain.doFilter(request, response);
        } finally {
            CorrelationIdUtil.clear();
            MDC.remove("correlationId");
        }
    }
}
