FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /app
COPY . .
ARG MODULE=dawn-app
RUN mvn clean package -DskipTests -pl ${MODULE} -am

FROM eclipse-temurin:25-jre
WORKDIR /app
ARG MODULE=dawn-app
COPY --from=builder /app/${MODULE}/target/${MODULE}-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8888
ENTRYPOINT [ "java", "-jar", "/app/app.jar" ]
