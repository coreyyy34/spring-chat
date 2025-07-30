package nz.coreyh.springchat.domain.model.token

import java.time.Instant

data class AccessToken(
    val jti: String,
    val userId: Int,
    val value: String,
    val expiresAt: Instant
)
