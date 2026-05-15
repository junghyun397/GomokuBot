FROM rust:alpine AS native-builder

RUN apk add --no-cache build-base \
    && rustup toolchain install nightly-2025-11-15 --profile minimal

WORKDIR /app

COPY native/mintaka .

# cached build
RUN --mount=type=cache,target=/usr/local/cargo/registry \
    --mount=type=cache,target=/usr/local/cargo/git \
    --mount=type=cache,target=/app/target \
    RUSTFLAGS="-C target-feature=-crt-static" cargo +nightly-2025-11-15 build --locked --release \
        -p rusty_renju_c \
        -p rusty_renju_image \
    && cp /app/target/release/librusty_renju_c.so /tmp/librusty_renju_c.so \
    && cp /app/target/release/librusty_renju_image.so /tmp/librusty_renju_image.so

FROM eclipse-temurin:25-jdk-alpine AS jvm-builder

WORKDIR /app

COPY . .

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon :discord:shadowJar \
    && cp discord/build/libs/discord-*-all.jar /tmp/gomokubot-discord.jar

FROM eclipse-temurin:25-jre-alpine AS runtime

WORKDIR /app

RUN apk add --no-cache fontconfig libgcc \
    && mkdir -p /usr/share/fonts

COPY --from=jvm-builder /tmp/gomokubot-discord.jar gomokubot-discord.jar
COPY --from=native-builder /tmp/librusty_renju_c.so native/mintaka/target/release/librusty_renju_c.so
COPY --from=native-builder /tmp/librusty_renju_image.so native/mintaka/target/release/librusty_renju_image.so
COPY core/fonts/CharterRegular.otf /usr/share/fonts/font.otf

ENTRYPOINT ["java", "--enable-native-access=ALL-UNNAMED", "-Xms2G", "-Xmx4G", "-jar", "/app/gomokubot-discord.jar"]
