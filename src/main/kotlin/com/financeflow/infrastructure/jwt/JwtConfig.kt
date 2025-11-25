package com.financeflow.infrastructure.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.server.config.*
import java.util.*

data class TokenConfig(
    val issuer: String,
    val audience: String,
    val realm: String,
    val secret: String,
    val expiration: Long
)

class JwtConfig(config: ApplicationConfig) {
    private val tokenConfig = TokenConfig(
        issuer = config.property("ktor.jwt.issuer").getString(),
        audience = config.property("ktor.jwt.audience").getString(),
        realm = config.property("ktor.jwt.realm").getString(),
        secret = config.property("ktor.jwt.secret").getString(),
        expiration = config.property("ktor.jwt.expiration").getString().toLong()
    )

    private val algorithm = Algorithm.HMAC256(tokenConfig.secret)

    fun createAccessToken(userId: String, role: String): String {
        return JWT.create()
            .withSubject("Authentication")
            .withIssuer(tokenConfig.issuer)
            .withAudience(tokenConfig.audience)
            .withClaim("userId", userId)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + tokenConfig.expiration))
            .sign(algorithm)
    }

    fun createRefreshToken(userId: String): String {
        return JWT.create()
            .withSubject("Refresh")
            .withIssuer(tokenConfig.issuer)
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + tokenConfig.expiration * 7)) // 7 days
            .sign(algorithm)
    }

    fun getVerifier(): JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(tokenConfig.issuer)
        .build()

    fun validateToken(token: String): DecodedJWT? {
        return try {
            getVerifier().verify(token)
        } catch (e: Exception) {
            null
        }
    }
}
