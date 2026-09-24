FROM docker.io/library/eclipse-temurin:17-jre
WORKDIR /app
COPY target/billing-service.jar app.jar
EXPOSE 8106
ENTRYPOINT ["java","-jar","/app/app.jar"]
