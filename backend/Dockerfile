FROM eclipse-temurin:25-jre
WORKDIR /app
ARG MODULE=dawn-app
COPY ${MODULE}/target/${MODULE}-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8888
ENTRYPOINT [ "java", "-jar", "/app/app.jar" ]
