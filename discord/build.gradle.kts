plugins {
    application
    idea
    id("com.gradleup.shadow") version "9.3.0"
}

repositories {
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io/")
}

dependencies {
    implementation(project(":utils"))
    implementation(project(":core"))

    implementation("net.dv8tion:JDA:5.6.1")
    implementation("com.github.minndevelopment:jda-ktx:0.12.0")
    implementation("com.github.minndevelopment:jda-reactor:1.6.0")

    implementation("ch.qos.logback:logback-classic:1.5.20")
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
