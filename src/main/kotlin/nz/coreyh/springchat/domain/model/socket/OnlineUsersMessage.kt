package nz.coreyh.springchat.domain.model.socket

import nz.coreyh.springchat.domain.model.SessionUser

data class OnlineUsersMessage(
    val users: List<nz.coreyh.springchat.domain.model.SessionUser>
)