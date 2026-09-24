package com.healthcare.inventoryservice.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/** Structured audit logging foundation (Phase 15 will ship to a sink). */
@Component
public class AuditService {
    private static final Logger log = LoggerFactory.getLogger("AUDIT");

    public void record(String user, String action, String entity, String entityId, String result) {
        log.info("audit user={} service=inventory-service action={} entity={} entityId={} correlationId={} result={}",
                user, action, entity, entityId, MDC.get("correlationId"), result);
    }
}
