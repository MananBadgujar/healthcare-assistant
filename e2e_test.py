import json, urllib.request, urllib.error

def register_user(username, password, role):
    url = 'http://localhost:8080/api/v1/auth/register'
    data = json.dumps({'firstName': 'Test', 'lastName': 'User', 'username': username, 'password': password, 'role': role}).encode()
    req = urllib.request.Request(url, data=data, headers={'Content-Type': 'application/json'})
    try:
        resp = urllib.request.urlopen(req)
        return resp.status, resp.read().decode()
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()

def login(username, password):
    url = 'http://localhost:8080/api/v1/auth/login'
    data = json.dumps({'username': username, 'password': password}).encode()
    req = urllib.request.Request(url, data=data, headers={'Content-Type': 'application/json'})
    try:
        resp = urllib.request.urlopen(req)
        raw = resp.read().decode()
        parsed = json.loads(raw)
        return resp.status, parsed.get('accessToken')
    except urllib.error.HTTPError as e:
        return e.code, None

def http_post(url, data, headers):
    req = urllib.request.Request(url, data=data, headers=headers)
    try:
        resp = urllib.request.urlopen(req)
        return resp.status, resp.read().decode()
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()

# Register ADMIN user
status, body = register_user('adminuser4', 'password', 'ADMIN')
print('ADMIN-REGISTER:', status, body)

# Login as ADMIN
status, access_token = login('adminuser4', 'password')
print('ADMIN-LOGIN:', status, 'token:', 'got' if access_token else 'FAIL')

if not access_token:
    print('Could not get access token, exiting.')
    exit(1)

headers = {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + access_token
}

# Create a provider
url = 'http://localhost:8080/api/v1/providers'
pdata = json.dumps({'name': 'Dr. Test', 'specialty': 'General Practice', 'facility': 'Test Hospital'}).encode()
status, body = http_post(url, pdata, headers)
print('PROVIDER-CREATE:', status, body)
if status == 201:
    provider = json.loads(body)
    provider_id = provider.get('id')
else:
    provider_id = None

# Create a patient
pdata2 = json.dumps({'firstName': 'Jane', 'lastName': 'Doe'}).encode()
url = 'http://localhost:8080/api/v1/patients'
status, body = http_post(url, pdata2, headers)
print('PATIENT-CREATE:', status, body)
if status == 201:
    patient = json.loads(body)
    patient_id = patient.get('id')
else:
    patient_id = None

# Try appointment creation
if provider_id and patient_id:
    url = 'http://localhost:8080/api/v1/appointments'
    adata = json.dumps({
        'patientId': patient_id,
        'providerId': provider_id,
        'startTime': '2026-01-15T10:00:00'
    }).encode()
    status, body = http_post(url, adata, headers)
    print('APPOINTMENT-CREATE:', status, body)

    appointment_id = None
    if status == 201:
        appointment = json.loads(body)
        appointment_id = appointment.get('id')

    # Verify appointment persistence
    if appointment_id:
        url = 'http://localhost:8080/api/v1/appointments/' + str(appointment_id)
        status, body = http_post(url, json.dumps({}).encode(), headers)
        print('GET-APPOINTMENT:', status, body)

    # Check Kafka for AppointmentCreated event - wait and consume
    import subprocess, time
    time.sleep(2)
    try:
        result = subprocess.run(
            ['docker', 'exec', 'platform-kafka-1', '/opt/kafka/bin/kafka-console-consumer.sh',
             '--bootstrap-server', 'localhost:9092',
             '--topic', 'healthcare.appointment.events.created',
             '--timeout-ms', '10000'],
            capture_output=True, text=True, timeout=15
        )
        print('KAFKA-CONSUMER returncode:', result.returncode)
        if result.stdout:
            print('EVENT OUTPUT:', result.stdout[:500])
        else:
            print('NO EVENTS CONSUMED - timeout or no messages')
    except Exception as ke:
        print('KAFKA CONSUMER ERROR:', ke)

    # Check notifications
    url = 'http://localhost:8080/api/v1/notifications'
    status, body = http_post(url, json.dumps({}).encode(), headers)
    print('NOTIFICATIONS:', status, body[:200])