FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/notification-service.jar app.jar
EXPOSE 8110
ENTRYPOINT ["java","-jar","/app/app.jar"]
