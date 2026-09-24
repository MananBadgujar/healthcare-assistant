FROM docker.io/library/eclipse-temurin:17-jre

WORKDIR /app

COPY target/auth-service.jar app.jar
COPY otel/opentelemetry-javaagent.jar opentelemetry-javaagent.jar

EXPOSE 8101

ENTRYPOINT ["java","-javaagent:/app/opentelemetry-javaagent.jar","-jar","/app/app.jar"]
