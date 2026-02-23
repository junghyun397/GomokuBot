plugins {
    application
    idea
    id("com.gradleup.shadow") version "9.3.0"
}

repositories {
    maven("https://jitpack.io/")
}

dependencies {
    implementation(project(":utils"))
    implementation(project(":core"))

    implementation("ch.qos.logback:logback-classic:1.5.20")
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
