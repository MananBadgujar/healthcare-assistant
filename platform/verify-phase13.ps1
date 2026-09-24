# Phase 13 live verification: gateway + services + E2E (requires built jars).
$ErrorActionPreference = "Continue"
function Check($name, $url, $method = "GET", $body = $null, $token = $null, $expect = 200) {
    try {
        $h = @{}
        if ($token) { $h["Authorization"] = "Bearer $token" }
        $h["X-Correlation-Id"] = "verify-$([guid]::NewGuid().ToString().Substring(0,8))"
        $params = @{Uri = $url; Method = $method; Headers = $h; TimeoutSec = 20}
        if ($body) { $params["Body"] = $body; $params["ContentType"] = "application/json" }
        $r = Invoke-WebRequest @params -UseBasicParsing
        if ($r.StatusCode -eq $expect) { Write-Host "PASS $name [$($r.StatusCode)]"; return $r }
        else { Write-Host "FAIL $name [got $($r.StatusCode), want $expect]"; return $null }
    } catch {
        $code = $_.Exception.Response.StatusCode.Value__ 2>$null
        if ($code -eq $expect) { Write-Host "PASS $name [$code]" }
        else { Write-Host "FAIL $name [$($_.Exception.Message)]" }
        return $null
    }
}
$GW = "http://localhost:8080"
Write-Host "=== gateway health ==="
Check "gateway-health" "$GW/gateway/health"
Write-Host "=== security: no token must be 401 ==="
Check "no-token-401" "$GW/api/v1/patients/1" "GET" $null $null 401
Write-Host "=== auth: register + login ==="
$reg = Check "register" "$GW/api/v1/auth/register" "POST" '{"username":"phase13user","password":"password123","role":"PATIENT"}' $null 201
$login = Check "login" "$GW/api/v1/auth/login" "POST" '{"username":"phase13user","password":"password123"}' $null 200
if ($login) {
    $tok = ($login.Content | ConvertFrom-Json).accessToken
    Write-Host "=== patient via gateway ==="
    $p = Check "patient-create" "$GW/api/v1/patients" "POST" '{"firstName":"Ada","lastName":"Lovelace","dateOfBirth":"1990-01-01","gender":"F"}' $tok 201
    Write-Host "=== provider via gateway ==="
    Check "provider-search" "$GW/api/v1/providers/search?specialty=cardio" "GET" $null $tok 200
    Write-Host "=== ai triage guardrails via gateway ==="
    Check "ai-triage" "$GW/api/v1/ai/triage" "POST" '{"symptoms":"I have chest pain"}' $tok 200
    Write-Host "=== cds via gateway (patient role must be 403) ==="
    Check "cds-forbidden" "$GW/api/v1/cds/check-interactions" "POST" '{"drugs":["warfarin","aspirin"]}' $tok 403
    Write-Host "=== rag via gateway ==="
    Check "rag-search" "$GW/api/v1/rag/search?q=health" "GET" $null $tok 200
}
Write-Host "=== done ==="
