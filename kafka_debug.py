import json, urllib.request, urllib.error, subprocess, time

# Login
data = json.dumps({'username': 'adminuser4', 'password': 'password'}).encode()
req = urllib.request.Request('http://localhost:8080/api/v1/auth/login', data=data, headers={'Content-Type': 'application/json'})
resp = urllib.request.urlopen(req)
raw = resp.read().decode()
parsed = json.loads(raw)
token = parsed.get('accessToken')

# Create appointment
adata = json.dumps({
    'patientId': 2,
    'providerId': 1,
    'startTime': '2026-05-15T10:00:00'
}).encode()
req = urllib.request.Request('http://localhost:8080/api/v1/appointments', data=adata, headers={
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + token
})
resp = urllib.request.urlopen(req)
appointment = json.loads(resp.read().decode())
appointment_id = appointment.get('id')
print('Appointment created:', appointment_id)

# Wait for Kafka event
time.sleep(5)

# Try consuming from Kafka
result = subprocess.run(
    ['docker', 'exec', 'platform-kafka-1', '/opt/kafka/bin/kafka-console-consumer.sh',
     '--bootstrap-server', 'localhost:9092',
     '--topic', 'healthcare.appointment.events.created',
     '--from-beginning',
     '--timeout-ms', '10000'],
    capture_output=True, text=True, timeout=15
)
print('Kafka consumer return code:', result.returncode)
if result.stdout:
    print('STDOUT:', result.stdout[:500])
else:
    print('No stdout output')
if result.stderr:
    print('STDERR:', result.stderr[:200])