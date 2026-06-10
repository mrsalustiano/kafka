package com.empresa.pedidos.exception;

public class RedisException extends RuntimeException {

    public RedisException(String message, Throwable cause) {
        super(message, cause);
    }
}
