package nz.coreyh.springchat.web.controller

import nz.coreyh.springchat.common.util.extractUser
import nz.coreyh.springchat.domain.model.socket.OnlineUsersMessage
import nz.coreyh.springchat.domain.model.socket.UserPresenceMessage
import nz.coreyh.springchat.domain.model.socket.UserPresenceType
import nz.coreyh.springchat.domain.service.WebSocketSessionService
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

private val logger = LoggerFactory.getLogger(WebSocketSessionController::class.java)

@Component
class WebSocketSessionController(
    private val webSocketSessionService: WebSocketSessionService,
    private val messagingTemplate: SimpMessagingTemplate
) {

    @EventListener
    fun handleSessionConnect(event: SessionConnectEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val sessionId = accessor.sessionId ?: return
        val user = accessor.extractUser() ?: return
        val hadExistingSession = webSocketSessionService.hasConnection(user)
        logger.info("User '${user.username}' (ID: ${user.id}) connected with session: $sessionId")

        val sessionUser = webSocketSessionService.addSession(sessionId, user)
        if (!hadExistingSession) {
            val presenceMessage = UserPresenceMessage(
                UserPresenceType.JOINED,
                sessionUser
            )
            messagingTemplate.convertAndSend("/topic/presence", presenceMessage)
        }
        val onlineUsersMessage =
            OnlineUsersMessage(webSocketSessionService.getOnlineUsers())
        messagingTemplate.convertAndSendToUser(user.username, "/queue/online-users", onlineUsersMessage)
    }

    @EventListener
    fun handleSessionDisconnect(event: SessionDisconnectEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val sessionId = accessor.sessionId ?: return
        val sessionUser = webSocketSessionService.removeSession(sessionId) ?: return
        logger.info("User '${sessionUser.username}' (ID: ${sessionUser.id}) disconnected with session: $sessionId")

        if (!webSocketSessionService.hasConnection(sessionUser)) {
            val presenceMessage = UserPresenceMessage(
                UserPresenceType.LEFT,
                sessionUser
            )
            messagingTemplate.convertAndSend("/topic/presence", presenceMessage)
        }
    }
}