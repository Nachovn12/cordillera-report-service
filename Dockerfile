FROM maven:4.0.0-rc-4-eclipse-temurin-25-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -DskipTests clean package

FROM eclipse-temurin:25-jre

RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
