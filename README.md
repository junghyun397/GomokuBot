# GomokuBot

## Quick Start

### gomokubot-discord

```shell
cd discord
docker build -t gomokubot-discord .
```

### gomokubot-telegram

### docker

```shell
echo "GOMOKUBOT_DISCORD_TOKEN=discordapplicationtoken
GOMOKUBOT_DISCORD_OFFICIAL_SERVER_ID=0
GOMOKUBOT_DISCORD_ARCHIVE_CHANNEL_ID=0
GOMOKUBOT_DISCORD_TESTER_ROLE_ID=0
" > .env
```

```shell
docker-compose up -d
```

```shell
docker-compose down
```
