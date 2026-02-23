plugins {
    idea
}

dependencies {
    implementation(project(":utils"))

    implementation("org.postgresql:r2dbc-postgresql:1.1.0.RELEASE")

    runtimeOnly("io.netty:netty-all:4.2.10.Final")
    runtimeOnly("io.netty:netty-tcnative-boringssl-static:2.0.75.Final")

    implementation("io.grpc:grpc-stub:1.79.0")
    implementation("io.grpc:grpc-netty:1.79.0")

    implementation("com.sksamuel.scrimage:scrimage-core:4.3.3")
}
