FROM alpine

RUN apk add openjdk11

WORKDIR /app
COPY build/libs/GomokuBot-all.jar .

ENTRYPOINT ["java", "-Xms2G", "-Xmx4G", "-jar", "/app/GomokuBot-all.jar"]
