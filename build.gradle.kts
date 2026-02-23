import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    application
    idea
    kotlin("jvm") version "2.3.10"
    // id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
    kotlin("plugin.serialization") version "2.3.10"
}

allprojects {
    group = "do1phin"
    version = "3.0-SNAPSHOT"

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://jitpack.io/")
    }

    tasks.withType<KotlinJvmCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    pluginManager.withPlugin("java") {
        extensions.getByType(JavaPluginExtension::class.java).toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(17)
    }
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
    }

    dependencies {
        implementation(kotlin("stdlib"))

        implementation("io.arrow-kt:arrow-core:2.0.1")

        implementation("com.github.junghyun397.kvine:renju_sjs1_2.13:fc91655c9a") // jitpack
        implementation("com.github.junghyun397.kvine:engine_3:fc91655c9a") // jitpack

        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.10.2")
        implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.3.0")

        implementation("com.google.guava:guava:33.5.0-jre")

        implementation("org.slf4j:slf4j-api:2.0.17")

        testImplementation(kotlin("test"))
    }
}
