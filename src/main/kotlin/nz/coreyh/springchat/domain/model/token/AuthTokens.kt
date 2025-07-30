package nz.coreyh.springchat.domain.model.token

data class AuthTokens(
    val accessToken: Token,
    val refreshToken: Token
)