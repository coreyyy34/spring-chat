package nz.coreyh.springchat.security.socket

import nz.coreyh.springchat.domain.service.UserService
import nz.coreyh.springchat.domain.service.JwtService
import nz.coreyh.springchat.common.util.CookieUtil
import org.slf4j.LoggerFactory
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

private val logger = LoggerFactory.getLogger(HandshakeInterceptor::class.java)

@Component
class HandshakeInterceptor(
    private val jwtService: JwtService,
    private val userService: UserService,
) : HandshakeInterceptor {

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {

        val servletRequest = (request as? ServletServerHttpRequest)?.servletRequest
            ?: return false

        val token = servletRequest.cookies
            ?.firstOrNull { it.name == CookieUtil.ACCESS_TOKEN_COOKIE_NAME }
            ?.value

        if (token == null) {
            logger.debug("No access token cookie found")
            return false
        }

        val claims = jwtService.validateAccessToken(token)
        if (claims == null) {
            logger.warn("Invalid or expired access token")
            return false
        }

        val user = userService.findById(claims.userId)
        if (user == null) {
            logger.warn("User not found with ID ${claims.userId} in access token")
            return false
        }

        val auth = UsernamePasswordAuthenticationToken(user, null, emptyList())
        attributes["auth"] = auth
        attributes["jwt_token"] = token
        attributes["user_id"] = claims.userId
        logger.info("WebSocket handshake successful for user: ${user.username}")
        return true
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) {
        if (exception != null) {
            logger.error("WebSocket handshake failed", exception)
        }
    }
}