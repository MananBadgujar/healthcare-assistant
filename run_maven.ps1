cd 'D:\java study material\healthcare-assistant\healthcare-assistant'
& .\mvnw clean test 2>&1 | Out-File -FilePath mvn_output_raw.txt -Encoding UTF8