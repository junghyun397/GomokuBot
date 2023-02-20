# GomokuBot
Now play Gomoku in your chat room. GomokuBot can do it. ― GomokuBot is an AI Chatbot designed to collect data for reinforcement learning. GomokuBot can be services with any platform and can handle many requests reliably based on reactive streams. 

*For the Renju engine and inference server used by GomokuBot, please refer to the [ResRenju](https://github.com/junghyun397/ResRenju) repository.*

## Features

* **Renju Rule Support.** With the ResRenju renju engine, GomokuBot always correctly applies [Renju rules](https://www.renju.net/rules/).
* **Multilingual Support.** Currently, six languages are supported: English, Korean, Korean, 日本語, 國漢文混用體, and Tiếng Việt. Thanks to the community translators. 
* **Fully Customizable.** Various elements such as board style, focus type, and hint type can be set.
* **Intuitive Input.** Within chatbot's environment, the ResRenju renju engine helps intuitive input.
* **Multiplatform Support.** Business logic and parser logic are completely separate. Just write a command parser to make GomokuBot service on various platforms.
* **Non-Blocking IO.** All IO tasks with Project Reactor and Coroutines. The database is also connected using the R2DBC SPI to ensure complete non-blocking IO.
* **Immutable, Functional.** Almost all parts of GomokuBot are composed of immutable objects, and business logic is programmed using monadic operations.

## Platforms

### gomokubot-discord

| help | in game | game result |
| ---  | ---     | ---         |
|![discord-help](https://github.com/junghyun397/GomokuBot/blob/master/images/discord-help.png?raw=true) | ![discord-in-game](https://github.com/junghyun397/GomokuBot/blob/master/images/discord-in-game.png?raw=true) | ![discord-finished-game](https://github.com/junghyun397/GomokuBot/blob/master/images/discord-finished-game.png?raw=true) |

Based on [JDA](https://github.com/DV8FromTheWorld/JDA), [jda-reactor](https://github.com/MinnDevelopment/jda-reactor), [jda-ktx](https://github.com/MinnDevelopment/jda-ktx). See [discord module](https://github.com/junghyun397/GomokuBot/tree/master/discord). You can invite gomokubot-discord bot via [this link](https://discord.com/api/oauth2/authorize?client_id=452520939792498689&permissions=137439266880&scope=bot%20applications.commands).

### gomokubot-telegram

WIP

### gomokubot-irc

WIP

## Quick Start

### build jar

```shell
gradlew build
```

### build DockerFile

```shell
cd discord
docker build -t gomokubot-discord .
```

### docker-compose

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

## Contributing
New features, bug fixes, new language support, GomokuBot welcome all forms of contribution. Feel free to open PR to contribute to this project. If you want to participate more deeply in this project and develop it together as a main contributor, please contact me.

## License
```text
BSD Zero Clause License (0BSD)

Copyright (C) 2022 by JeongHyeon Choi <me@do1ph.in>

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH
REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY
AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR
OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
PERFORMANCE OF THIS SOFTWARE.
```
