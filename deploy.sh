#!/bin/bash

./gradlew build

cd discord
docker build -t gomokubot-discord .

cd ..
docker compose down
docker compose up -d

