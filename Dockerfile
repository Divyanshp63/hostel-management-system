# Stage 1: Build the Spring Boot application using Maven and OpenJDK 17
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal Java 17 runtime container
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV PORT=2007
EXPOSE 2007
ENTRYPOINT ["java", "-jar", "app.jar"]
