FROM docker.io/library/eclipse-temurin:17-jre
WORKDIR /app
COPY target/patient-service.jar app.jar
EXPOSE 8102
ENTRYPOINT ["java","-jar","/app/app.jar"]
