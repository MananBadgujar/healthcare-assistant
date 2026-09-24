FROM docker.io/library/eclipse-temurin:17-jre
WORKDIR /app
COPY target/provider-service.jar app.jar
EXPOSE 8103
ENTRYPOINT ["java","-jar","/app/app.jar"]
