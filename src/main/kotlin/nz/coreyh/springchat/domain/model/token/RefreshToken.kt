package nz.coreyh.springchat.domain.model.token

import java.time.Instant

data class RefreshToken(
    val id: Int,
    val jti: String,
    val userId: Int,
    val expiresAt: Instant,
    val revoked: Boolean,
    val used: Boolean
) {

    fun isExpired(): Boolean {
        return expiresAt.isBefore(Instant.now())
    }

    fun isUsable(): Boolean {
        return !revoked && !used && !isExpired()
    }
}