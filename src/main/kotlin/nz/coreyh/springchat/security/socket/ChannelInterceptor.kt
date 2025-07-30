package nz.coreyh.springchat.security.socket

import nz.coreyh.springchat.domain.service.JwtService
import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.stereotype.Component

private val logger = LoggerFactory.getLogger(ChannelInterceptor::class.java)

@Component
class ChannelInterceptor(
    private val jwtService: JwtService,
) : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
            ?: return null
        if (accessor.command != StompCommand.SEND) {
            return message
        }

        val sessionAttributes = accessor.sessionAttributes
        val token = sessionAttributes?.get("jwt_token") as? String
        val userId = sessionAttributes?.get("user_id") as? Int
        if (token == null || userId == null) {
            logger.warn("Missing token or user ID in session")
            return null
        }

        val claims = jwtService.validateAccessToken(token)
        if (claims == null || claims.userId != userId) {
            logger.info("Token expired for user ID: $userId")
            return null
        }
        logger.info("Authentication validated for user ID: $userId")
        return message
    }
}