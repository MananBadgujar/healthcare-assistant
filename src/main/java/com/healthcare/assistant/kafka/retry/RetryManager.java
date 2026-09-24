package com.healthcare.assistant.kafka.retry;

import com.healthcare.assistant.kafka.event.EventEnvelope;
import com.healthcare.assistant.kafka.exception.RetryableException;
import com.healthcare.assistant.kafka.exception.NonRetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
public class RetryManager {

    private static final Logger logger = LoggerFactory.getLogger(RetryManager.class);

    private static final int MAX_RETRIES = 3;
    private static final long[] BACKOFF_DELAYS = {1000, 2000, 4000}; // 1s, 2s, 4s

    public <T extends Throwable> void execute(Runnable action, Class<T> retryableExceptionType) {
        execute(action, MAX_RETRIES, retryableExceptionType);
    }

    public <T extends Throwable> void execute(Runnable action, int maxRetries, Class<T> retryableExceptionType) {
        int attempts = 0;
        while (attempts <= maxRetries) {
            try {
                action.run();
                logger.info("Action succeeded on attempt {}", (attempts + 1));
                return;
            } catch (Exception e) {
                if (retryableExceptionType.isInstance(e)) {
                    attempts++;
                    if (attempts <= maxRetries) {
                        long delay = BACKOFF_DELAYS[attempts - 1];
                        logger.warn("Retryable exception on attempt {}: {}. Retrying in {}ms...",
                                attempts, e.getMessage(), delay);
                        try {
                            TimeUnit.MILLISECONDS.sleep(delay);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            logger.error("Retry interrupted", ie);
                            throw new RuntimeException(ie);
                        }
                    } else {
                        logger.error("Max retries ({}) exceeded for: {}", maxRetries, e.getMessage());
                        throw new RetryableException("Max retries exceeded: " + e.getMessage(), e);
                    }
                } else {
                    // Non-retryable exception - throw immediately
                    throw new NonRetryableException("Non-retryable exception: " + e.getMessage(), e);
                }
            }
        }
        throw new RetryableException("Max retries exceeded", new RuntimeException("Max retries exceeded after " + maxRetries + " attempts"));
    }
}