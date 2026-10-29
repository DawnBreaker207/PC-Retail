FROM eclipse-temurin:25-jre
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
ARG MODULE=dawn-app
COPY ${MODULE}/target/${MODULE}-0.0.1-SNAPSHOT.jar /app/app.jar

# JVM profile mặc định tối ưu footprint bộ nhớ cho container
ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx256m -XX:+UseSerialGC -XX:TieredStopAtLevel=1"

EXPOSE 8888
ENTRYPOINT [ "java", "-jar", "/app/app.jar" ]
