import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    idea
    kotlin("jvm") version "1.5.10"
    // id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
    kotlin("plugin.serialization") version "1.6.21"
}

allprojects {
    group = "do1phin"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        mavenLocal()
        google()
        maven("https://jitpack.io/")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
    }

    dependencies {
        implementation(kotlin("stdlib"))

//        implementation("jrenju:jrenju_2.13:0.1.0-SNAPSHOT")
        implementation("com.github.junghyun397.Kvine:jrenju_2.13:e3940c1216")

        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")

        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.0")
        implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.1.5")

        implementation("com.google.guava:guava:31.1-jre")

        implementation("org.slf4j:slf4j-api:1.7.36")

        testImplementation(kotlin("test"))
    }
}
