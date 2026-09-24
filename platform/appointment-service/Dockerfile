FROM docker.io/library/eclipse-temurin:17-jre
WORKDIR /app
COPY target/appointment-service.jar app.jar
EXPOSE 8104
ENTRYPOINT ["java","-jar","/app/app.jar"]
