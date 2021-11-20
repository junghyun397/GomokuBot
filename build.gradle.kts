import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.ofSourceSet
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

val group = "me.junghyun397"
val version = "1.0-SNAPSHOT"

val grpcVersion = "1.42.1"
val grpcKotlinVersion = "1.2.0"
val protobufVersion = "3.19.1"

plugins {
    application
    kotlin("jvm") version "1.5.10"
    id("com.google.protobuf") version "0.8.16"
    id("org.jlleitschuh.gradle.ktlint") version "9.2.1"
}

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io/")
    google()
}

dependencies {
    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("com.google.protobuf:protobuf-kotlin:$protobufVersion")

    implementation("net.dv8tion:JDA:4.3.0_346")
    implementation("com.github.minndevelopment:jda-ktx:1223d5cbb8a8caac6d28799a36001f1844d7aa7d")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk7@jar"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
}

application {
    mainClass.set("MainKt")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}