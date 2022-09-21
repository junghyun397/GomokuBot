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
    implementation("com.github.minndevelopment:jda-ktx:0.9.2-alpha.13")
    implementation("com.github.minndevelopment:jda-reactor:1.5.0")

    implementation("ch.qos.logback:logback-classic:1.4.0")
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
