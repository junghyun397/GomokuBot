FROM openjdk:11-jdk-slim

WORKDIR /app
COPY build/libs/GomokuBot-all.jar .

ENTRYPOINT ["java", "-Xms2G", "-Xmx4G", "-jar", "/app/GomokuBot-all.jar"]
