package com.empresa.clientes.util;

import com.empresa.clientes.dto.PageResponse;
import com.empresa.clientes.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageResponseUtilTest {

    @Test
    void of_deveCalcularMetadados() {
        PageResponse<String> page = PageResponseUtil.of(List.of("a", "b"), 0, 10, 20);

        assertThat(page.totalPages()).isEqualTo(2);
        assertThat(page.hasNext()).isTrue();
        assertThat(page.hasPrevious()).isFalse();
        assertThat(page.first()).isTrue();
        assertThat(page.last()).isFalse();
    }

    @Test
    void of_quandoVazio_deveMarcarEmpty() {
        PageResponse<String> page = PageResponseUtil.of(List.of(), 0, 10, 0);

        assertThat(page.empty()).isTrue();
        assertThat(page.last()).isTrue();
    }

    @Test
    void resolveSortColumn_padrao() {
        assertThat(PageResponseUtil.resolveSortColumn(null)).isEqualTo("codigo_cliente");
    }

    @Test
    void resolveSortColumn_valido() {
        assertThat(PageResponseUtil.resolveSortColumn("nome,asc")).isEqualTo("nome");
    }

    @Test
    void resolveSortColumn_invalido_deveLancarExcecao() {
        assertThatThrownBy(() -> PageResponseUtil.resolveSortColumn("invalido"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void resolveSortDirection_padrao() {
        assertThat(PageResponseUtil.resolveSortDirection(null)).isEqualTo("ASC");
    }

    @Test
    void resolveSortDirection_desc() {
        assertThat(PageResponseUtil.resolveSortDirection("email,desc")).isEqualTo("DESC");
    }

    @Test
    void of_quandoSizeZero_deveCalcularTotalPagesZero() {
        PageResponse<String> page = PageResponseUtil.of(List.of(), 0, 0, 10);

        assertThat(page.totalPages()).isZero();
    }

    @Test
    void resolveSortDirection_invalido_deveLancarExcecao() {
        assertThatThrownBy(() -> PageResponseUtil.resolveSortDirection("nome,invalid"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void of_quandoPaginaIntermediaria_deveTerHasNextEHasPrevious() {
        PageResponse<String> page = PageResponseUtil.of(List.of("a"), 1, 10, 30);

        assertThat(page.hasNext()).isTrue();
        assertThat(page.hasPrevious()).isTrue();
        assertThat(page.first()).isFalse();
        assertThat(page.last()).isFalse();
    }

    @Test
    void of_quandoUltimaPagina_deveMarcarLast() {
        PageResponse<String> page = PageResponseUtil.of(List.of("a"), 2, 10, 25);

        assertThat(page.last()).isTrue();
        assertThat(page.hasNext()).isFalse();
    }

    @Test
    void resolveSortColumn_quandoBlank_deveRetornarPadrao() {
        assertThat(PageResponseUtil.resolveSortColumn("   ")).isEqualTo("codigo_cliente");
    }

    @Test
    void resolveSortDirection_quandoSortSemVirgula_deveRetornarAsc() {
        assertThat(PageResponseUtil.resolveSortDirection("nome")).isEqualTo("ASC");
    }
}
