// Este archivo configura la resolución de plugins y dependencias globales.

pluginManagement {
    // 1. Repositorios para buscar PLUGINS (como Ktor y Kotlin)
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    // 2. Declaración de versiones de los PLUGINS para que sean resueltos primero
    plugins {
        // Kotlin y Ktor
        kotlin("jvm") version "2.0.0"
        id("io.ktor.jvm") version "2.3.7"

        // Serialización y Koin
        kotlin("plugin.serialization") version "2.0.0"
        // CRÍTICO para Koin/Ktorm: Asegura que las clases puedan ser extendidas/inyectadas
        id("org.jetbrains.kotlin.plugin.allopen") version "2.0.0"
        id("org.koin.gradle") version "3.2.0"

        // Migración de Base de Datos
        id("org.flywaydb.flyway") version "9.22.3"
    }
}

dependencyResolutionManagement {
    // 3. Repositorios para buscar DEPENDENCIAS (como Ktorm, PostgreSQL)
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
        // CRÍTICO: Necesario para que Gradle encuentre las dependencias de Ktorm (JitPack)
        maven("https://jitpack.io")
    }
}

rootProject.name = "financeflow-api"