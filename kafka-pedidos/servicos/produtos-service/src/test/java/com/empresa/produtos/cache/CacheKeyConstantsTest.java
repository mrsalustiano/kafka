package com.empresa.produtos.cache;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CacheKeyConstantsTest {

    @Test
    void produtoKey_deveFormatarChave() {
        assertThat(CacheKeyConstants.produtoKey(10L)).isEqualTo("produto:10");
        assertThat(CacheKeyConstants.PRODUTO_ALL).isEqualTo("produto:all");
    }
}
