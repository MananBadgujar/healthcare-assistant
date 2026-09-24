package com.healthcare.assistant.rag.vector;

import java.util.Locale;

/**
 * Stateless vector helpers used by the {@link VectorStore} and the query path.
 * <p>
 * Embeddings are stored as space-separated floats so the same schema works on
 * MySQL and H2; this class centralises the (de)serialisation and similarity
 * math so the storage and search code stays silent on those details.
 */
public final class VectorMath {

    private VectorMath() {
    }

    /** Serialise a float vector to a space-separated string. */
    public static String encode(float[] vector) {
        if (vector == null || vector.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(vector.length * 8);
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(Float.toString(vector[i]));
        }
        return sb.toString();
    }

    /** Decode a serialised vector back to a float array. */
    public static float[] decode(String value) {
        if (value == null || value.isBlank()) {
            return new float[0];
        }
        String[] parts = value.trim().split("\\s+");
        float[] vector = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vector[i] = Float.parseFloat(parts[i]);
        }
        return vector;
    }

    /**
     * Cosine similarity between two vectors. Returns {@code 0.0} if either
     * vector is empty or zero-norm, which keeps the search rank stable without
     * producing {@code NaN}.
     */
    public static double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length == 0 || b.length == 0 || a.length != b.length) {
            return 0.0;
        }
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /** Human-friendly compact rendering used in logs only. */
    public static String brief(float[] vector) {
        if (vector == null || vector.length == 0) {
            return "[]";
        }
        return String.format(Locale.ROOT, "[%d dims:%.4f...]", vector.length, vector[0]);
    }
}
