FROM openjdk:21-jdk-slim

WORKDIR /app

COPY build/libs/search-discovery-service-*.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "app.jar"]
