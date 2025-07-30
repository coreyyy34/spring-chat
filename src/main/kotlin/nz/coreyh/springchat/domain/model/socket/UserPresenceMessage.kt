package nz.coreyh.springchat.domain.model.socket

import nz.coreyh.springchat.domain.model.SessionUser

data class UserPresenceMessage(
    val type: nz.coreyh.springchat.domain.model.socket.UserPresenceType,
    val user: nz.coreyh.springchat.domain.model.SessionUser
)

enum class UserPresenceType {
    JOINED,
    LEFT
}