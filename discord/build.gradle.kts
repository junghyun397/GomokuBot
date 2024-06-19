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

    implementation("net.dv8tion:JDA:5.0.0-beta.24")
    implementation("com.github.minndevelopment:jda-ktx:e8b0a7080cc234727abf5a83871ae0c9b6603a58")
    implementation("com.github.minndevelopment:jda-reactor:e01a635")

    implementation("ch.qos.logback:logback-classic:1.4.12")
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
