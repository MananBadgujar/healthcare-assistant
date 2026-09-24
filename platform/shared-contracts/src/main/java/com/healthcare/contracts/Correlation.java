package com.healthcare.contracts;

import java.util.UUID;

/** Correlation-id helpers. Header name is shared by gateway and all services. */
public final class Correlation {
    public static final String HEADER = "X-Correlation-Id";
    private Correlation() {}

    public static String ensure(String incoming) {
        return (incoming == null || incoming.isBlank()) ? UUID.randomUUID().toString() : incoming;
    }
}
