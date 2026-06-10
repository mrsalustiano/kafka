package com.empresa.broker.exception;

public class KafkaConsumeException extends RuntimeException {

    public KafkaConsumeException(String message, Throwable cause) {
        super(message, cause);
    }
}
