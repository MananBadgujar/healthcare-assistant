import subprocess, time

# Produce a test message
result = subprocess.run(
    ['docker', 'exec', 'platform-kafka-1', 'sh', '-c',
     'printf \"{\\\"test\\\":\\\"msg\\\"}\\n\" | /opt/kafka/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic healthcare.appointment.events.created'],
    capture_output=True, text=True, timeout=10
)
print('Producer result:', result.returncode)
print('Producer stdout:', result.stdout[:200] if result.stdout else 'empty')
print('Producer stderr:', result.stderr[:200] if result.stderr else 'empty')

# Wait a moment
time.sleep(2)

# Consume
result2 = subprocess.run(
    ['docker', 'exec', 'platform-kafka-1', '/opt/kafka/bin/kafka-console-consumer.sh',
     '--bootstrap-server', 'localhost:9092',
     '--topic', 'healthcare.appointment.events.created',
     '--from-beginning',
     '--timeout-ms', '5000'],
    capture_output=True, text=True, timeout=10
)
print('Consumer result:', result2.returncode)
print('Consumer stdout:', result2.stdout[:200] if result2.stdout else 'empty')
print('Consumer stderr:', result2.stderr[:200] if result2.stderr else 'empty')