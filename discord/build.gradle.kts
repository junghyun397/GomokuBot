plugins {
    application
    idea
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io/")
}

dependencies {
    implementation(project(":utils"))
    implementation(project(":core"))

    implementation("net.dv8tion:JDA:5.0.0-alpha.17")
    implementation("com.github.minndevelopment:jda-ktx:f9422f40132a6638f852c5d16f44cfbc6ad50af1")
    implementation("com.github.minndevelopment:jda-reactor:1.5.0")

    implementation("ch.qos.logback:logback-classic:1.2.11")
}

application {
    mainClass.set("discord.AppKt")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "discord.AppKt"
        )
    }
}
