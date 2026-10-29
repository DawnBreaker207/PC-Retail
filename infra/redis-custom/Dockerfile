FROM eclipse-temurin:25-jre
WORKDIR /app
COPY target/redis-server.jar /app/redis-server.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "/app/redis-server.jar"]
