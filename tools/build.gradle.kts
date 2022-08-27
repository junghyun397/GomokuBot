plugins {
    application
    idea
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    maven("https://jitpack.io/")
}

dependencies {
    implementation(project(":utils"))
    implementation(project(":core"))

    implementation("io.r2dbc:r2dbc-postgresql:0.8.12.RELEASE")
    implementation("mysql:mysql-connector-java:8.0.30")

    implementation("ch.qos.logback:logback-classic:1.2.11")
}

application {
    mainClass.set("tools.migration.migrateDatabaseKt")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "discord.AppKt"
        )
    }
}
