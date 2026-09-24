# Phase 13 Microservices — Local Runbook

## Layout
- `api-gateway/` — single external entry point (port 8080)
- `auth-service/` (8101), `patient-service/` (8102), `provider-service/` (8103),
  `appointment-service/` (8104), `medication-service/` (8105), `billing-service/` (8106),
  `ai-service/` (8107), `rag-service/` (8108), `cds-service/` (8109),
  `notification-service/` (8110), `inventory-service/` (8111)
- `shared-contracts/` — DomainEvent envelope, ApiError, correlation + topic names
- Monolith (repo root, Phase 1-12) is preserved and reachable as gateway fallback.

## Database-per-service
Each service uses its own isolated H2 database (`./data/<service>-db` locally;
separate filesystem/volume per container in Docker). No service has entities or
datasources belonging to another service — ownership is enforced by packaging:
each service module only contains its own `entity/` + `repo/` packages.

## Build
```
.\mvnw.cmd -f platform/pom.xml package -DskipTests
.\mvnw.cmd -f platform/pom.xml test        # unit + integration (incl. embedded-Kafka)
```

## Run locally (no Docker)
Start services in order (each on its own port), then the gateway:
```
java -jar platform\auth-service\target\auth-service.jar
java -jar platform\patient-service\target\patient-service.jar
... (all services)
java -jar platform\api-gateway\target\api-gateway.jar
```
Kafka is optional at runtime: producers/consumers tolerate a missing broker
(events are skipped with a warning) so the REST + gateway surface works without it.
With Docker, Kafka (KRaft) is provided — see below.

## Run with Docker
```
mvn -f platform/pom.xml package -DskipTests
docker compose -f platform/docker-compose.yml up --build
```
Inside compose, services address each other by service name
(`http://patient-service:8102`, `kafka:9092`). No localhost hardcoding:
every downstream URL is an env var (`*_URL`, `KAFKA_BOOTSTRAP`).

## Verify live
```
powershell -File platform\verify-phase13.ps1
```
Covers: gateway health, 401 without token, register/login, patient create via
gateway, provider search, AI triage guardrails, CDS RBAC, RAG search.

## E2E flow
Client -> Gateway (JWT + correlation) -> Domain service -> own DB -> Kafka event
-> notification-service consumer -> stored notification (idempotent).
