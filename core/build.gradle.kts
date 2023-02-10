
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.ofSourceSet
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import org.gradle.kotlin.dsl.invoke

val grpcVersion = "1.44.1"
val grpcKotlinVersion = "1.2.1"
val protobufVersion = "3.19.4"

plugins {
    idea
    id("com.google.protobuf") version "0.8.16"
}

dependencies {
    implementation(project(":utils"))

    implementation("org.postgresql:r2dbc-postgresql:1.0.0.RELEASE")

    runtimeOnly("io.netty:netty-all:4.1.86.Final")
    runtimeOnly("io.netty:netty-tcnative-boringssl-static:2.0.54.Final")

    implementation("io.grpc:grpc-stub:1.48.0")
    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("com.google.protobuf:protobuf-kotlin:$protobufVersion")
    implementation("io.grpc:grpc-netty:1.48.0")

    implementation("com.sksamuel.scrimage:scrimage-core:4.0.33")
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
