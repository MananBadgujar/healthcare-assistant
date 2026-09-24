package com.healthcare.assistant.service.impl;

import com.healthcare.assistant.dto.*;
import com.healthcare.assistant.entity.*;
import com.healthcare.assistant.repository.*;
import com.healthcare.assistant.service.*;
import com.healthcare.assistant.entity.enums.InvoiceStatus;
import com.healthcare.assistant.repository.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.*;

@Service
public class HealthDomainGuardService {

    /**
     * Keywords indicative of non-health topics that should be rejected.
     */
    private static final Set<String> NON_HEALTH_KEYWORDS = Set.of(
            "password", "log", "sign", "in", "register", "forgot",
            "reset", "code", "program", "software", "development", "algorithm",
            "server", "port", "database", "query", "api", "endpoint",
            "test", "unit", "mock", "stub", "stubs",
            "calculate", "math", "geometry", "physics", "chemistry",
            "economics", "finance", "accounting", "statistics", "data",
            "opencode", "github", "commit", "push", "pull", "branch",
            "issue", "pr", "review", "merge", "conflict", "release",
            "documentation", "wiki", "readme", "license",
            "design", "architecture", "pattern", "factory", "singleton",
            "deploy", "build", "clean", "install", "maven", "gradle",
            "docker", "container", "kubernetes", "helm",
            "question", "answer", "quiz", "homework", "assignment",
            "education", "course", "student", "teacher", "lecture",
            "shopping", "buy", "order", "price", "discount", "cart",
            "restaurant", "menu", "food", "recipe", "cook", "eat",
            "music", "song", "movie", "film", "play", "theater",
            "travel", "flight", "hotel", "ticket", "reservation",
            "entertainment", "game", "stream", "video", "tv",
            "weather", "sports", "news", "politics", "opinion"
    );

    /**
     * Checks if the given query is health-related.
     *
     * @param query the user input
     * @return true if query is health-related
     * @throws IllegalArgumentException if query is non-health
     */
    public boolean isHealthQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Query cannot be null or empty");
        }

        // Simple heuristic: if any non-health keyword appears, treat as non-health
        // This is a basic approach; can be enhanced with NLP classification.
        boolean containsNonHealth = NON_HEALTH_KEYWORDS.stream()
                .anyMatch(keyword -> query.toLowerCase().contains(keyword));

        if (containsNonHealth) {
            // Optionally, we could throw or just return false; for now return false.
            return false;
        }
        return true;
    }
}