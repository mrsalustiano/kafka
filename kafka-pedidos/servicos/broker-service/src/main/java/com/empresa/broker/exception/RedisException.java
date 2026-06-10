package com.empresa.broker.exception;

public class RedisException extends RuntimeException {

    public RedisException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedisException(String message) {
        super(message);
    }
}
