package com.healthcare.cdsservice.web;

import com.healthcare.cdsservice.service.CdsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cds")
public class CdsController {
    private final CdsService cds;
    public CdsController(CdsService cds) { this.cds = cds; }

    @PostMapping("/check-interactions")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF')")
    public Map<String, Object> check(@RequestBody Map<String, List<String>> body) {
        return cds.check(body.get("drugs"));
    }

    @PostMapping("/contraindications/check")
    @PreAuthorize("hasAnyRole('ADMIN','PROVIDER','STAFF')")
    public Map<String, Object> contra(@RequestBody Map<String, Object> body) {
        return Map.of("alerts", List.of(), "requiresProviderReview", true);
    }
}
