FROM eclipse-temurin:25-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY target/dawn-ai-0.0.1-SNAPSHOT-exec.jar app.jar
EXPOSE 8087
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD curl -f http://localhost:8087/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
