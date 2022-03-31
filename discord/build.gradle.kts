plugins {
    application
    idea
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    implementation(project(":utils"))
    implementation(project(":core"))

    implementation("net.dv8tion:JDA:5.0.0-alpha.9")
    implementation("com.github.minndevelopment:jda-ktx:652775540cf5832ef03e5f25e80c4448390b4fa1")
    implementation("com.github.minndevelopment:jda-reactor:1.5.0")

    implementation("ch.qos.logback:logback-classic:1.2.11")
}

application {
    mainClass.set("AppKt")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "AppKt"
        )
    }
}
