import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    application
    idea
    kotlin("jvm") version "2.4.20"
    // id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
    kotlin("plugin.serialization") version "2.4.20" apply false
}

allprojects {
    group = "do1phin"
    version = "4.0-SNAPSHOT"

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://jitpack.io/")
    }

    tasks.withType<KotlinJvmCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_26)
        }
    }

    pluginManager.withPlugin("java") {
        extensions.getByType(JavaPluginExtension::class.java).toolchain.languageVersion.set(JavaLanguageVersion.of(26))
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(26)
    }

    tasks.withType<Test>().configureEach {
        jvmArgs("--enable-native-access=ALL-UNNAMED")
    }

    tasks.withType<JavaExec>().configureEach {
        jvmArgs("--enable-native-access=ALL-UNNAMED")
    }
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
    }

    dependencies {
        implementation(kotlin("stdlib"))

        implementation("io.arrow-kt:arrow-core:2.2.3")

        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")

        testImplementation(kotlin("test"))
    }
}
