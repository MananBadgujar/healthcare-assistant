package com.healthcare.assistant.kafka.exception;

@SuppressWarnings("serial")
public class RetryableException extends RuntimeException {

    public RetryableException(String message, Throwable cause) {
        super(message, cause);
    }
}