import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.ofSourceSet
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

val group = "do1phin"
val version = "1.0-SNAPSHOT"

val grpcVersion = "1.44.1"
val grpcKotlinVersion = "1.2.1"
val protobufVersion = "3.19.4"

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
    implementation("dev.miku:r2dbc-mysql:0.8.2.RELEASE")

    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("com.google.protobuf:protobuf-kotlin:$protobufVersion")

    implementation("net.dv8tion:JDA:5.0.0-alpha.9")
    implementation("com.github.minndevelopment:jda-ktx:652775540cf5832ef03e5f25e80c4448390b4fa1")
    implementation("com.github.minndevelopment:jda-reactor:1.5.0")
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
    mainClass.set("AppKt")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}