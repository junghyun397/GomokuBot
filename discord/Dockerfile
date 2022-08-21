FROM alpine

RUN apk add openjdk11
RUN apk add fontconfig
RUN mkdir /usr/share/fonts
RUN wget -O /usr/share/fonts/font.otf https://github.com/junghyun397/GomokuBot/blob/master/core/fonts/CharterRegular.otf?raw=true

WORKDIR /app
COPY build/libs/discord-*-all.jar gomokubot-discord.jar

ENTRYPOINT ["java", "-Xms2G", "-Xmx4G", "-jar", "/app/gomokubot-discord.jar"]
