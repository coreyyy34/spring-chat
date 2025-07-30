package nz.coreyh.springchat.common.util

import nz.coreyh.springchat.domain.model.User
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import java.security.Principal

private val logger = LoggerFactory.getLogger("UserExtensions")

fun StompHeaderAccessor.extractUser(): User? {
    val principal = this.user ?: run {
        logger.warn("No principal found in StompHeaderAccessor")
        return null
    }
    return principal.extractUser()
}

fun Principal.extractUser(): User? {
    if (this !is UsernamePasswordAuthenticationToken) {
        logger.warn("Invalid principal type: ${this.javaClass.name ?: "null"}")
        return null
    }
    val principal = this.principal
    if (principal !is User) {
        logger.warn("Invalid user principal type: ${principal?.javaClass?.name ?: "null"}")
        return null
    }
    return principal
}