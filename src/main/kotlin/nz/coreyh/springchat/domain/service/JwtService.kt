package nz.coreyh.springchat.domain.service

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.MacAlgorithm
import jakarta.annotation.PostConstruct
import nz.coreyh.springchat.domain.model.token.Token
import nz.coreyh.springchat.domain.model.token.TokenClaims
import nz.coreyh.springchat.domain.model.token.TokenType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import javax.crypto.SecretKey

private val logger = LoggerFactory.getLogger(JwtService::class.java)

@Service
class JwtService(
    @Value("\${jwt.access.secret}") private val accessSecret: String,
    @Value("\${jwt.refresh.secret}") private val refreshSecret: String
) {

    private lateinit var accessKey: SecretKey
    private lateinit var refreshKey: SecretKey

    companion object {
        val SIGNATURE_ALGORITHM: MacAlgorithm = Jwts.SIG.HS256
    }

    @PostConstruct
    fun initKeys() {
        require(accessSecret.isNotBlank()) { "Access secret cannot be blank" }
        require(refreshSecret.isNotBlank()) { "Refresh secret cannot be blank" }

        accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret))
        refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret))
        logger.info("JWT keys initialized.")
    }

    fun generateAccessToken(id: Int) =
        generateToken(id, accessKey, TokenType.ACCESS)

    fun generateRefreshToken(id: Int) =
        generateToken(id, refreshKey, TokenType.REFRESH)

    fun validateAccessToken(token: String): TokenClaims? =
        extractClaims(token, accessKey)

    fun validateRefreshToken(token: String): TokenClaims? =
        extractClaims(token, refreshKey)

    private fun generateToken(userId: Int, key: SecretKey, tokenType: TokenType): Token {
        val now = Instant.now()
        val expiresAt = now.plusMillis(tokenType.lifetime.inWholeMilliseconds)
        val jti = UUID.randomUUID().toString()
        val token = Jwts.builder()
            .subject(userId.toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .id(jti)
            .claim("typ", tokenType.typeName)
            .signWith(key, SIGNATURE_ALGORITHM)
            .compact()
        return Token(
            jti = jti,
            userId = userId,
            value = token,
            expiresAt = expiresAt,
            type = tokenType,
        )
    }

    private fun extractClaims(token: String, key: SecretKey): TokenClaims? {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload.let {
                    TokenClaims(
                        jti = it.id,
                        userId = Integer.parseInt(it.subject),
                        type = TokenType.fromTypeName(
                            it.get(
                                "typ",
                                String::class.java
                            )
                        ),
                    )
                }
        } catch (e: ExpiredJwtException) {
            logger.warn("Token expired: ${e.message}")
            null
        } catch (e: MalformedJwtException) {
            logger.warn("Malformed token: ${e.message}")
            null
        } catch (e: Exception) {
            logger.warn("Invalid token: ${e.message}")
            null
        }
    }
}