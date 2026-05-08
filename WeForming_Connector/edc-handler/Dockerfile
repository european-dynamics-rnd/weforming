FROM eclipse-temurin:21-jdk-noble

WORKDIR /app

COPY edc_handler-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 15588

ENTRYPOINT ["java", "-jar", "app.jar"]