# GomokuBot

Rewrite GomokuBot in Kotlin - WIP

## Getting Started

### Build

```shell
./gradlew build
```

### Environment Variables
```shell
export GOMOKUBOT_DISCORD_TOKEN=TOKEN
```

### Docker-Compose

```shell
docker build -t gomokubot .
docker-compose up -d
```

### Docker

```shell
docker build -t gomokubot .
docker run --name gomokubot gomokubot
```

### Direct

```shell
java -jar build/libs/GomokuBot-all.jar
```
