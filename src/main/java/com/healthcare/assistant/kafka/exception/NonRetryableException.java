package com.healthcare.assistant.kafka.exception;

@SuppressWarnings("serial")
public class NonRetryableException extends RuntimeException {

    public NonRetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}