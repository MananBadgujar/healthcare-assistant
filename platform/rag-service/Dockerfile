FROM docker.io/library/eclipse-temurin:17-jre
WORKDIR /app
COPY target/rag-service.jar app.jar
EXPOSE 8108
ENTRYPOINT ["java","-jar","/app/app.jar"]
