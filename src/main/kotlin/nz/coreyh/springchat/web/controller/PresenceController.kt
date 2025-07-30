package nz.coreyh.springchat.web.controller

import nz.coreyh.springchat.config.Routes
import nz.coreyh.springchat.domain.model.SessionUser
import nz.coreyh.springchat.domain.service.WebSocketSessionService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

private val logger = LoggerFactory.getLogger(HistoryController::class.java)

@RestController
class PresenceController(
    private val webSocketSessionService: WebSocketSessionService
) {

    @GetMapping(Routes.Api.Presence.ONLINE_USERS)
    fun getOnlineUsers(): List<SessionUser> {
        logger.info("GET ${Routes.Api.Presence.ONLINE_USERS}")
        return webSocketSessionService.getOnlineUsers()
    }
}