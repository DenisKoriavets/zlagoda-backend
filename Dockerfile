FROM gradle:8.7-jdk21-alpine AS builder
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY gradlew ./
RUN ./gradlew dependencies --no-daemon || true

COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]