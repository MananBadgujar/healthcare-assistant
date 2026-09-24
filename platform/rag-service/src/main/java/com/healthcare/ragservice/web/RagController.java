package com.healthcare.ragservice.web;

import com.healthcare.ragservice.entity.KbDocument;
import com.healthcare.ragservice.service.RagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rag")
public class RagController {
    private final RagService rag;
    public RagController(RagService rag) { this.rag = rag; }

    @PostMapping("/ingest")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','PROVIDER')")
    public ResponseEntity<Map<String, Object>> ingest(@RequestBody Map<String, String> body) {
        KbDocument d = rag.ingest(body.get("title"), body.get("content"));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", d.getId(), "title", d.getTitle()));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF','PATIENT')")
    public List<Map<String, Object>> search(@RequestParam("q") String q,
                                            @RequestParam(defaultValue = "3") int topK) {
        return rag.search(q, Math.min(topK, 10));
    }

    @PostMapping("/ask")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF','PATIENT')")
    public Map<String, Object> ask(@RequestBody Map<String, String> body) {
        List<Map<String, Object>> hits = rag.search(body.getOrDefault("q", ""), 3);
        return Map.of("answer", hits.isEmpty() ? "No relevant knowledge found." : "See cited sources.",
                "citations", hits);
    }
}
