package nz.coreyh.springchat.domain.service

import nz.coreyh.springchat.domain.model.SessionUser
import nz.coreyh.springchat.domain.model.User
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class WebSocketSessionService {

    private val sessionUsers = ConcurrentHashMap<String, SessionUser>()

    fun addSession(sessionId: String, user: User): SessionUser {
        val sessionUser = SessionUser(user.id, user.username)
        sessionUsers[sessionId] = sessionUser
        return sessionUser
    }

    fun removeSession(sessionId: String): SessionUser? {
        return sessionUsers.remove(sessionId)
    }

    fun hasConnection(user: User) = sessionUsers.any { it.value.id == user.id }

    fun hasConnection(user: SessionUser) = sessionUsers.any { it.value.id == user.id }

    fun getOnlineUsers(): List<SessionUser> = synchronized(sessionUsers) {
        sessionUsers.values
            .groupBy { it.id }
            .map { it.value.first() }
    }
}