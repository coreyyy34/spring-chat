package nz.coreyh.springchat.domain.model.dto

data class AuthRegisterRequest(
    val username: String,
    val password: String,
    val confirmPassword: String,
)