package com.empresa.clientes.audit;

import com.empresa.clientes.repository.AuditoriaRepository;
import com.empresa.clientes.util.CorrelationIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private static final String ENTIDADE_CLIENTE = "CLIENTE";

    private final AuditoriaRepository auditoriaRepository;

    public void registrar(OperacaoAuditoria operacao, Long idEntidade, String detalhes) {
        try {
            auditoriaRepository.registrar(
                    operacao.name(),
                    ENTIDADE_CLIENTE,
                    idEntidade,
                    CorrelationIdUtil.get(),
                    detalhes
            );
        } catch (Exception ex) {
            log.error("Falha ao registrar auditoria para cliente {}: {}", idEntidade, ex.getMessage());
        }
    }

    public void registrarErro(Long idEntidade, String detalhes) {
        registrar(OperacaoAuditoria.ERRO, idEntidade, detalhes);
    }
}
