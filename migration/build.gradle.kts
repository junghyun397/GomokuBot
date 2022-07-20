plugins {
    application
    idea
}

repositories {
    maven("https://jitpack.io/")
}

dependencies {
    implementation(project(":utils"))
    implementation(project(":core"))

    implementation("io.r2dbc:r2dbc-postgresql:0.8.12.RELEASE")
    implementation("mysql:mysql-connector-java:8.0.29")

    implementation("ch.qos.logback:logback-classic:1.2.11")
}
