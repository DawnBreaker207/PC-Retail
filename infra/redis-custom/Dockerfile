FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline -B
COPY src ./src
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=builder /app/target/redis-server.jar /app/redis-server.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "/app/redis-server.jar"]
