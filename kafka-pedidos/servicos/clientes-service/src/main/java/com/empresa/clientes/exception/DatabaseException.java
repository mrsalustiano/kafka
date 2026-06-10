package com.empresa.clientes.exception;

public class DatabaseException extends RuntimeException {

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
