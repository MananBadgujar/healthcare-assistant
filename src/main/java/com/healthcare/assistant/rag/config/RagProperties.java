package com.healthcare.assistant.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Externalised configuration for the Phase 9 RAG patient-education pipeline.
 * <p>
 * Binds to the {@code rag.*} properties. Every tunable of the pipeline
 * (chunking, embedding, vector search, context window, LLM, timeouts) is
 * captured here so nothing is hardcoded in the services.
 */
@Validated
@ConfigurationProperties(prefix = "rag")
public class RagProperties {

    /** Positive ingestion settings. */
    private Ingestion ingestion = new Ingestion();

    /** Retrieval settings. */
    private Retrieval retrieval = new Retrieval();

    /** Generation / LLM settings. */
    private Generation generation = new Generation();

    /** Embedding settings. */
    private Embedding embedding = new Embedding();

    /** Conversation settings. */
    private Conversation conversation = new Conversation();

    public Ingestion getIngestion() { return ingestion; }
    public void setIngestion(Ingestion ingestion) { this.ingestion = ingestion; }

    public Retrieval getRetrieval() { return retrieval; }
    public void setRetrieval(Retrieval retrieval) { this.retrieval = retrieval; }

    public Generation getGeneration() { return generation; }
    public void setGeneration(Generation generation) { this.generation = generation; }

    public Embedding getEmbedding() { return embedding; }
    public void setEmbedding(Embedding embedding) { this.embedding = embedding; }

    public Conversation getConversation() { return conversation; }
    public void setConversation(Conversation conversation) { this.conversation = conversation; }

    public static class Ingestion {
        /** Chunk size in characters. */
        private int chunkSize = 1000;
        /** Overlap between adjacent chunks in characters. */
        private int chunkOverlap = 200;
        /** Maximum allowed source document size in characters after extraction. */
        private int maxDocumentChars = 200_000;

        public int getChunkSize() { return chunkSize; }
        public void setChunkSize(int chunkSize) { this.chunkSize = chunkSize; }
        public int getChunkOverlap() { return chunkOverlap; }
        public void setChunkOverlap(int chunkOverlap) { this.chunkOverlap = chunkOverlap; }
        public int getMaxDocumentChars() { return maxDocumentChars; }
        public void setMaxDocumentChars(int maxDocumentChars) { this.maxDocumentChars = maxDocumentChars; }
    }

    public static class Retrieval {
        /** Number of top-K chunks to retrieve. */
        private int topK = 5;
        /** Minimum cosine similarity a chunk must reach to be considered relevant. */
        private double similarityThreshold = 0.20;
        /** If true, only active documents/chunks are considered during retrieval. */
        private boolean activeOnly = true;

        public int getTopK() { return topK; }
        public void setTopK(int topK) { this.topK = topK; }
        public double getSimilarityThreshold() { return similarityThreshold; }
        public void setSimilarityThreshold(double similarityThreshold) { this.similarityThreshold = similarityThreshold; }
        public boolean isActiveOnly() { return activeOnly; }
        public void setActiveOnly(boolean activeOnly) { this.activeOnly = activeOnly; }
    }

    public static class Generation {
        /** LLM model name used for patient-education answers (overrides default when set). */
        private String model;
        /** Maximum approximate tokens (characters) of retrieved evidence sent to the LLM. */
        private int maxContextChars = 4000;
        /** Whether to fall back to the deterministic safety message when the LLM is unavailable. */
        private boolean fallbackOnLlmError = true;

        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public int getMaxContextChars() { return maxContextChars; }
        public void setMaxContextChars(int maxContextChars) { this.maxContextChars = maxContextChars; }
        public boolean isFallbackOnLlmError() { return fallbackOnLlmError; }
        public void setFallbackOnLlmError(boolean fallbackOnLlmError) { this.fallbackOnLlmError = fallbackOnLlmError; }
    }

    public static class Embedding {
        /**
         * Embedding implementation to use: "hash" (default, deterministic local)
         * or "ollama" (HTTP call to Ollama embeddings endpoint).
         */
        private String provider = "hash";
        /** Dimension of the deterministic hashing embedding. */
        private int dimension = 256;
        /** Ollama embedding model name when provider=ollama. */
        private String ollamaModel = "nomic-embed-text";

        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        public int getDimension() { return dimension; }
        public void setDimension(int dimension) { this.dimension = dimension; }
        public String getOllamaModel() { return ollamaModel; }
        public void setOllamaModel(String ollamaModel) { this.ollamaModel = ollamaModel; }
    }

    public static class Conversation {
        /** Maximum number of previous messages retained and sent to the LLM as history. */
        private int historyWindow = 4;

        public int getHistoryWindow() { return historyWindow; }
        public void setHistoryWindow(int historyWindow) { this.historyWindow = historyWindow; }
    }

    /** Convenience: list of all categories supported by the knowledge base. */
    public java.util.List<String> supportedCategories() {
        return java.util.Arrays.stream(com.healthcare.assistant.entity.enums.KbCategory.values())
                .map(Enum::name)
                .collect(java.util.stream.Collectors.toList());
    }
}
