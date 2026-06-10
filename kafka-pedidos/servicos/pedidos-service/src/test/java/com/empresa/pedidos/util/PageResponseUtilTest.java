package com.empresa.pedidos.util;

import com.empresa.pedidos.dto.PageResponse;
import com.empresa.pedidos.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageResponseUtilTest {

    @Test
    void of_deveMontarPagina() {
        PageResponse<String> page = PageResponseUtil.of(List.of("a"), 0, 10, 1);

        assertThat(page.content()).containsExactly("a");
        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.first()).isTrue();
        assertThat(page.last()).isTrue();
    }

    @Test
    void of_quandoVazio_deveIndicarEmpty() {
        PageResponse<String> page = PageResponseUtil.of(List.of(), 0, 10, 0);

        assertThat(page.empty()).isTrue();
        assertThat(page.totalPages()).isZero();
    }

    @Test
    void resolveSortColumn_padrao() {
        assertThat(PageResponseUtil.resolveSortColumn(null)).isEqualTo("codigo_pedido");
    }

    @Test
    void resolveSortColumn_valido() {
        assertThat(PageResponseUtil.resolveSortColumn("status,ASC")).isEqualTo("status");
    }

    @Test
    void resolveSortColumn_invalido() {
        assertThatThrownBy(() -> PageResponseUtil.resolveSortColumn("invalido,ASC"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void resolveSortDirection_padrao() {
        assertThat(PageResponseUtil.resolveSortDirection(null)).isEqualTo("ASC");
    }

    @Test
    void resolveSortDirection_desc() {
        assertThat(PageResponseUtil.resolveSortDirection("codigo_pedido,DESC")).isEqualTo("DESC");
    }

    @Test
    void resolveSortDirection_invalido() {
        assertThatThrownBy(() -> PageResponseUtil.resolveSortDirection("codigo_pedido,XYZ"))
                .isInstanceOf(ValidationException.class);
    }
}
