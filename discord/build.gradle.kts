plugins {
    application
    idea
    id("com.gradleup.shadow") version "9.6.1"
}

repositories {
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io/")
}

dependencies {
    implementation(project(":utils"))
    implementation(project(":core"))

    implementation("net.dv8tion:JDA:6.7.0")
    implementation("club.minnced:jda-ktx:0.15.0")

    implementation("org.slf4j:slf4j-api:2.0.20")
    implementation("ch.qos.logback:logback-classic:1.6.5")
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
