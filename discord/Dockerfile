FROM alpine

RUN apk add openjdk11

WORKDIR /app
COPY build/libs/discord-*-all.jar gomokubot-discord.jar

ENTRYPOINT ["java", "-Xms2G", "-Xmx4G", "-jar", "/app/gomokubot-discord.jar"]
