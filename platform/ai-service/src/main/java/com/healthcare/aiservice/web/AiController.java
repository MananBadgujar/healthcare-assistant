package com.healthcare.aiservice.web;

import com.healthcare.aiservice.common.AuditService;
import com.healthcare.aiservice.common.EventPublisher;
import com.healthcare.aiservice.service.AiService;
import com.healthcare.contracts.DomainEvent;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {
    private final AiService ai;
    private final EventPublisher events;
    private final AuditService audit;
    public AiController(AiService ai, EventPublisher events, AuditService audit) {
        this.ai = ai; this.events = events; this.audit = audit;
    }

    @PostMapping("/triage")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF','PATIENT')")
    public ResponseEntity<Map<String, Object>> triage(@RequestBody Map<String, String> body,
                                                      Authentication auth,
                                                      @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Map<String, Object> out = ai.triage(body.get("symptoms"), authHeader);
        audit.record(auth.getName(), "TRIAGE", "Triage", "-", "SUCCESS");
        try {
            events.publish("healthcare.ai.events.triage-completed",
                    DomainEvent.of("ai.triage-completed", "Triage", auth.getName(), "ai-service",
                            MDC.get("correlationId"), Map.of("urgency", String.valueOf(out.get("urgency")))));
        } catch (Exception ignored) {}
        return ResponseEntity.ok(out);
    }

    @PostMapping("/lab-interpret")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF')")
    public Map<String, Object> lab(@RequestBody Map<String, String> body) {
        return ai.labInterpret(body.get("text"));
    }
}
