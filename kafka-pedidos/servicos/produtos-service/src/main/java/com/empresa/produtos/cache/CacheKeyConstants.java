package com.empresa.produtos.cache;

public final class CacheKeyConstants {

    public static final String PRODUTO_PREFIX = "produto:";
    public static final String PRODUTO_ALL = "produto:all";

    private CacheKeyConstants() {
    }

    public static String produtoKey(Long id) {
        return PRODUTO_PREFIX + id;
    }
}
