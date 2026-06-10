package com.empresa.produtos.audit;

import com.empresa.produtos.repository.AuditoriaRepository;
import com.empresa.produtos.util.CorrelationIdUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock
    private AuditoriaRepository auditoriaRepository;

    @InjectMocks
    private AuditoriaService auditoriaService;

    @AfterEach
    void tearDown() {
        CorrelationIdUtil.clear();
    }

    @Test
    void registrar_devePersistirAuditoria() {
        CorrelationIdUtil.set("corr-1");

        auditoriaService.registrar(OperacaoAuditoria.CREATE, 1L, "detalhe");

        verify(auditoriaRepository).registrar(
                eq("CREATE"), eq("PRODUTO"), eq(1L), eq("corr-1"), eq("detalhe"));
    }

    @Test
    void registrar_quandoErro_naoDevePropagar() {
        doThrow(new RuntimeException("erro")).when(auditoriaRepository)
                .registrar(anyString(), anyString(), anyLong(), any(), anyString());

        auditoriaService.registrar(OperacaoAuditoria.UPDATE, 1L, "detalhe");
    }

    @Test
    void registrarErro_deveUsarOperacaoErro() {
        auditoriaService.registrarErro(1L, "falha");

        verify(auditoriaRepository).registrar(
                eq("ERRO"), eq("PRODUTO"), eq(1L), eq(null), eq("falha"));
    }
}
