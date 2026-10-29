FROM eclipse-temurin:25-jre
WORKDIR /app
COPY target/redis-server.jar /app/redis-server.jar

# Redis-custom siêu nhẹ: chỉ cần 64m - 128m
ENV JAVA_TOOL_OPTIONS="-Xms64m -Xmx128m -XX:+UseSerialGC -XX:TieredStopAtLevel=1"

EXPOSE 8888
ENTRYPOINT ["java", "-jar", "/app/redis-server.jar"]
