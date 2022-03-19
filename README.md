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
docker run --name mysql -e MYSQL_ROOT_PASSWORD=1q2w3e4r! -d -p 3306:3306 mysql
docker build -t gomokubot .
docker run --name gomokubot gomokubot
```

### Direct

```shell
java -jar build/libs/GomokuBot-all.jar
```
