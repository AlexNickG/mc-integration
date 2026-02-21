FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/mc-integration-0.0.1-SNAPSHOT.jar app.jar

ENV EUREKA_HOST=http://eureka:8761/eureka/

CMD ["java", "-jar", "app.jar"]