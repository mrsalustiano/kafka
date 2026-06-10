package com.empresa.pedidos.cache;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CacheKeyConstantsTest {

    @Test
    void produtoKey_deveFormatarChave() {
        assertThat(CacheKeyConstants.produtoKey(42L)).isEqualTo("produto:42");
    }
}
