package nz.coreyh.springchat.domain.model

import java.io.Serializable

data class User(
    val id: Int,
    val username: String,
    val password: String
) : Serializable