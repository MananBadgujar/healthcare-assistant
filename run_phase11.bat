@echo off
set HEALTHCARE_OLLAMA_E2E=true
call mvnw test -q 2>&1 | findstr /REI "Tests run:.*BUILD" >> run.log.txt
echo %ERRORLEVEL% > exitcode.txt