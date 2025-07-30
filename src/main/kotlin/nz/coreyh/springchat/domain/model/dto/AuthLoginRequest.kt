package nz.coreyh.springchat.domain.model.dto

data class AuthLoginRequest(
    val username: String,
    val password: String
)