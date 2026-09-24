FROM docker.io/library/eclipse-temurin:17-jre
WORKDIR /app
COPY target/medication-service.jar app.jar
EXPOSE 8105
ENTRYPOINT ["java","-jar","/app/app.jar"]
