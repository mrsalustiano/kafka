package com.empresa.produtos.audit;

import com.empresa.produtos.repository.AuditoriaRepository;
import com.empresa.produtos.util.CorrelationIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private static final String ENTIDADE_PRODUTO = "PRODUTO";

    private final AuditoriaRepository auditoriaRepository;

    public void registrar(OperacaoAuditoria operacao, Long idEntidade, String detalhes) {
        try {
            auditoriaRepository.registrar(
                    operacao.name(),
                    ENTIDADE_PRODUTO,
                    idEntidade,
                    CorrelationIdUtil.get(),
                    detalhes
            );
        } catch (Exception ex) {
            log.error("Falha ao registrar auditoria para produto {}: {}", idEntidade, ex.getMessage());
        }
    }

    public void registrarErro(Long idEntidade, String detalhes) {
        registrar(OperacaoAuditoria.ERRO, idEntidade, detalhes);
    }
}
