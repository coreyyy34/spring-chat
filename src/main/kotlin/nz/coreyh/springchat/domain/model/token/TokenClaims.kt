package nz.coreyh.springchat.domain.model.token

data class TokenClaims(
    val jti: String,
    val userId: Int,
    val type: TokenType,
)