FROM docker.io/library/eclipse-temurin:17-jre
WORKDIR /app
COPY target/ai-service.jar app.jar
EXPOSE 8107
ENTRYPOINT ["java","-jar","/app/app.jar"]
