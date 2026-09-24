Write-Host "Starting auth-service..."

# Change to auth-service directory
Set-Location "D:\java study material\healthcare-assistant\healthcare-assistant\platform\auth-service"

# Clear old H2 data
Remove-Item "data\auth-service-db.mv.db" -ErrorAction SilentlyContinue
Remove-Item "data\auth-service-db.trace" -ErrorAction SilentlyContinue

# Run maven
Write-Host "Running maven..."
$mvnw = "D:\java study material\healthcare-assistant\healthcare-assistant\platform\auth-service\mvnw.cmd"
cmd /c "$mvnw spring-boot:run" > "D:\auth-service-startup.log" 2>&1

Write-Host "auth-service startup complete"