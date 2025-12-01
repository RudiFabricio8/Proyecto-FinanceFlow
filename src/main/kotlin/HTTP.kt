package com.financeflow.plugins

import com.asyncapi.kotlinasyncapi.ktor.AsyncApiPlugin
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.httpsredirect.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import org.slf4j.event.Level

/**
 * Configura las funcionalidades esenciales de red y documentación para la API.
 * Esto incluye manejo de JSON, CORS para frontend, logging de peticiones,
 * redireccionamiento seguro (HTTPS) y documentación (OpenAPI/AsyncAPI).
 */
fun Application.configureHTTP() {
    // 1. Content Negotiation (Serialización JSON)
    // Transforma automáticamente los objetos Kotlin en JSON y viceversa.
    install(ContentNegotiation) {
        json()
    }

    // 2. CORS (Cross-Origin Resource Sharing) Configuration
    // Permite al frontend acceder a la API desde diferentes puertos/dominios.
    install(CORS) {
        // Métodos HTTP permitidos, cubriendo CRUD completo
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)

        // Headers permitidos, cruciales para autenticación y tipo de contenido
        allowHeader(HttpHeaders.Authorization) // Para tokens JWT o de sesión
        allowHeader(HttpHeaders.ContentType)   // Para enviar/recibir JSON
        allowHeader("MyCustomHeader")

        // Necesario si se usan cookies o credenciales de autenticación
        allowCredentials = true

        // CRÍTICO: Permitimos cualquier host en desarrollo.
        // ¡REEMPLAZAR en producción por dominios específicos!
        anyHost()

        // Tiempo que el navegador puede almacenar en caché la respuesta preflight de CORS
        maxAgeInSeconds = 3600
    }

    // 3. Call Logging (Registro de peticiones)
    // Útil para debugging y monitorización del tráfico.
    install(CallLogging) {
        level = Level.INFO // Nivel de detalle de los logs
    }

    // 4. HTTPS Redirect
    // Redirige automáticamente el tráfico HTTP no seguro al puerto SSL (443).
    install(HttpsRedirect) {
        sslPort = 443
        permanentRedirect = true
    }

    // 5. Configuración de Documentación
    routing {
        // Genera la documentación de la API REST (Swagger UI) en /openapi
        openAPI(path = "openapi")
    }

    // 6. AsyncAPI Configuration
    // Documentación para servicios basados en eventos o mensajería asíncrona.
    install(AsyncApiPlugin) {
        extension = AsyncApiExtension.builder<Unit>(Unit) {
            info {
                title("Finance Flow API - Eventos")
                version("1.0.0")
                description("Documentación para mensajes asíncronos y eventos.")
            }
        }
    }
}