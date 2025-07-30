package nz.coreyh.springchat.domain.model.token

import java.time.Instant

data class Token(
    val jti: String,
    val userId: Int,
    val value: String,
    val expiresAt: Instant,
    val type: TokenType,
) {
    fun isExpired() = expiresAt < Instant.now()
}
