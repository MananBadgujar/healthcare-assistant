FROM docker.io/library/eclipse-temurin:17-jre
WORKDIR /app
COPY target/cds-service.jar app.jar
EXPOSE 8109
ENTRYPOINT ["java","-jar","/app/app.jar"]
