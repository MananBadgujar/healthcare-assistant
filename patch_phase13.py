"""Patch all platform services: 401 entry point, kafka fast-fail in tests, auth route narrowing; update test expectations."""
import os, re
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "platform")
SERVICES = ["auth-service","patient-service","provider-service","appointment-service",
            "medication-service","billing-service","ai-service","rag-service",
            "cds-service","notification-service","inventory-service"]

def pkg(svc):
    return "com.healthcare." + svc.replace("-", "")

ENTRY_IMPORTS = """import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthcare.contracts.ApiError;
import com.healthcare.contracts.Correlation;
import jakarta.servlet.http.HttpServletResponse;
"""

for svc in SERVICES:
    P = pkg(svc)
    path = os.path.join(ROOT, svc, f"src/main/java/{P.replace('.','/')}/config/SecurityConfig.java")
    src = open(path, encoding="utf-8").read()
    # add imports
    src = src.replace("import org.springframework.security.config.http.SessionCreationPolicy;",
                      ENTRY_IMPORTS + "import org.springframework.security.config.http.SessionCreationPolicy;")
    # narrow auth-service public paths
    if svc == "auth-service":
        src = src.replace('.requestMatchers("/api/v1/auth/**").permitAll()',
                          '.requestMatchers("/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()');
    # add entry point handling
    old_chain = """            .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);"""
    new_chain = """            .exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, e) -> {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType("application/json");
                Object cid = req.getAttribute(Correlation.HEADER);
                new ObjectMapper().writeValue(res.getOutputStream(),
                        new ApiError(401, "UNAUTHORIZED", "Missing or invalid token",
                                cid == null ? null : String.valueOf(cid), "%s"));
            }))
            .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);""" % svc
    assert old_chain in src, svc
    src = src.replace(old_chain, new_chain)
    open(path, "w", encoding="utf-8", newline="\n").write(src)
    print("patched security", svc)

    # test properties: kafka fast-fail
    tp = os.path.join(ROOT, svc, "src/test/resources/application.properties")
    tprops = open(tp, encoding="utf-8").read()
    extra = """spring.kafka.admin.auto-create=false
spring.kafka.admin.properties.request.timeout.ms=2000
spring.kafka.admin.properties.default.api.timeout.ms=3000
spring.kafka.producer.properties.max.block.ms=1000
spring.kafka.producer.properties.request.timeout.ms=2000
spring.kafka.producer.properties.delivery.timeout.ms=3000
"""
    if "max.block.ms" not in tprops:
        tprops += extra
        open(tp, "w", encoding="utf-8", newline="\n").write(tprops)
    print("patched test props", svc)

# ---- update test expectations 403 -> 401 for unauthenticated cases ----
def patch_test(svc, fname, pairs):
    P = pkg(svc)
    path = os.path.join(ROOT, svc, f"src/test/java/{P.replace('.','/')}/{fname}")
    src = open(path, encoding="utf-8").read()
    for old, new in pairs:
        assert old in src, (svc, old)
        src = src.replace(old, new)
    open(path, "w", encoding="utf-8", newline="\n").write(src)
    print("patched test", svc, fname)

patch_test("auth-service", "AuthFlowTest.java", [
    ('mvc.perform(get("/api/v1/auth/me")).andExpect(status().isForbidden());',
     'mvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());'),
    ('mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer invalid"))\n                .andExpect(status().isForbidden());',
     'mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer invalid"))\n                .andExpect(status().isUnauthorized());'),
])
patch_test("patient-service", "PatientApiTest.java", [
    ('mvc.perform(get("/api/v1/patients/" + id)).andExpect(status().isForbidden());',
     'mvc.perform(get("/api/v1/patients/" + id)).andExpect(status().isUnauthorized());'),
])
patch_test("provider-service", "ProviderApiTest.java", [
    ('mvc.perform(get("/api/v1/providers/search")).andExpect(status().isForbidden());',
     'mvc.perform(get("/api/v1/providers/search")).andExpect(status().isUnauthorized());'),
])
patch_test("appointment-service", "AppointmentApiTest.java", [
    ('mvc.perform(get("/api/v1/appointments/" + id)).andExpect(status().isForbidden());',
     'mvc.perform(get("/api/v1/appointments/" + id)).andExpect(status().isUnauthorized());'),
])
patch_test("ai-service", "AiApiTest.java", [
    ('mvc.perform(post("/api/v1/ai/triage")).andExpect(status().isForbidden());',
     'mvc.perform(post("/api/v1/ai/triage")).andExpect(status().isUnauthorized());'),
])
print("ALL PATCHES DONE")
