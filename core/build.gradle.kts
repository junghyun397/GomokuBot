plugins {
    idea
    id("org.jooq.jooq-codegen-gradle") version "3.21.5"
}

val jooqVersion = "3.21.5"
val jooqGeneratedDir = layout.buildDirectory.dir("generated-src/jooq/main")

dependencies {
    implementation(project(":utils"))

    implementation("org.jooq:jooq:$jooqVersion")
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

    jooqCodegen("org.jooq:jooq-meta-extensions:$jooqVersion")
}

kotlin {
    sourceSets {
        main {
            kotlin.srcDir(jooqGeneratedDir)
        }
    }
}

jooq {
    configuration {
        logging = org.jooq.meta.jaxb.Logging.WARN

        generator {
            name = "org.jooq.codegen.KotlinGenerator"

            database {
                name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                inputSchema = "PUBLIC"
                isOutputSchemaToDefault = true

                properties {
                    property {
                        key = "scripts"
                        value = rootProject.file("schema.sql").absolutePath
                    }
                    property {
                        key = "sort"
                        value = "none"
                    }
                    property {
                        key = "unqualifiedSchema"
                        value = "public"
                    }
                    property {
                        key = "defaultNameCase"
                        value = "lower"
                    }
                }
            }

            generate {
                isDeprecated = false
            }

            target {
                packageName = "core.database.jooq"
                directory = jooqGeneratedDir.get().asFile.absolutePath
            }
        }
    }
}

tasks.named("compileKotlin") {
    dependsOn(tasks.named("jooqCodegen"))
}
