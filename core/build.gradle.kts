plugins {
    idea
}

dependencies {
    implementation(project(":utils"))

    implementation("org.postgresql:r2dbc-postgresql:1.1.1.RELEASE")

    val ktorVersion = "3.4.3"

    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-sse:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp-jvm:${ktorVersion}")

    runtimeOnly("io.netty:netty-all:4.2.10.Final")
    runtimeOnly("io.netty:netty-tcnative-boringssl-static:2.0.75.Final")

    implementation("com.sksamuel.scrimage:scrimage-core:4.3.3")
}
