val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.2.20"
    id("io.ktor.plugin") version "3.3.2"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20"
    application
}

kotlin {
    jvmToolchain(21)
}

ktor {
    fatJar {
        archiveFileName.set("financeflow-api-all.jar")
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

group = "com.financeflow"
version = "1.0.0"

application {
    mainClass = "com.financeflow.ApplicationKt"
}

dependencies {
    val ktorVersion = "3.3.2"
    val ktormVersion = "3.6.0"
    val postgresqlVersion = "42.7.1"
    val hikariCpVersion = "5.0.1"
    val flywayVersion = "10.11.0"
    val jwtVersion = "4.5.0"
    val swaggerVersion = "2.2.22"

    // Ktor Core
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-config-yaml:$ktorVersion")

    // Content Negotiation
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")

    // Request Validation
    implementation("io.ktor:ktor-server-request-validation:$ktorVersion")

    // Status Pages
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")

    // Authentication
    implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktorVersion")
    implementation("com.auth0:java-jwt:$jwtVersion")

    // CORS & Logging
    implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // OpenAPI / Swagger
    implementation("io.ktor:ktor-server-openapi:$ktorVersion")
    implementation("io.ktor:ktor-server-swagger:$ktorVersion")
    implementation("io.swagger.core.v3:swagger-core-jakarta:$swaggerVersion")
    implementation("io.swagger.core.v3:swagger-models:$swaggerVersion")
    implementation("io.swagger.core.v3:swagger-annotations:$swaggerVersion")
    implementation("io.swagger.core.v3:swagger-jaxrs2:$swaggerVersion")

    // Security
    implementation("org.mindrot:jbcrypt:0.4")

    // Database
    implementation("org.ktorm:ktorm-core:$ktormVersion")
    implementation("org.ktorm:ktorm-support-postgresql:$ktormVersion")
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("com.zaxxer:HikariCP:$hikariCpVersion")

    // Database Migrations
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    implementation("org.flywaydb:flyway-core:$flywayVersion")

    // Additional Utilities
    implementation("com.typesafe:config:1.4.3")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
    implementation("org.valiktor:valiktor-core:0.12.0")

    // Dependency Injection
    implementation("io.insert-koin:koin-ktor:3.5.3")
    implementation("io.insert-koin:koin-logger-slf4j:3.5.3")

    // Testing
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("org.testcontainers:postgresql:1.19.6")
    testImplementation("org.testcontainers:junit-jupiter:1.19.6")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
}


tasks.test {
    useJUnitPlatform()
}
