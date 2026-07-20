FROM maven:latest AS builder
WORKDIR /app

COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=builder /app/dawn-app/target/dawn-app-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8888
ENTRYPOINT [ "java", "-jar", "/app/app.jar" ]
