package com.empresa.pedidos.cache;

public final class CacheKeyConstants {

    public static final String PRODUTO_PREFIX = "produto:";

    private CacheKeyConstants() {
    }

    public static String produtoKey(Long id) {
        return PRODUTO_PREFIX + id;
    }
}
