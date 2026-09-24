@echo off
set HEALTHCARE_OLLAMA_E2E=true
call .\mvnw.cmd test -Dtest=com.healthcare.assistant.rag.OllamaRealEndToEndIntegrationTest --fail-at-end 2>&1
echo.
echo.
EXIT /B %ERRORLEVEL%