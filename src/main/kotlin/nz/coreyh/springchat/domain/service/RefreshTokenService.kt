package nz.coreyh.springchat.domain.service

import nz.coreyh.springchat.domain.model.token.Token
import nz.coreyh.springchat.persistence.repository.RefreshTokenRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

private val logger = LoggerFactory.getLogger(RefreshTokenService::class.java)

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepository
) {

    fun storeToken(token: Token) {
        logger.debug("Persisting new refresh token with jti='{}' and expiry={}", token.jti, token.expiresAt)
        refreshTokenRepository.add(token)
    }

    fun isTokenValid(jti: String): Boolean {
        val token = refreshTokenRepository.findByJti(jti)
        if (token != null) {
            if (!token.isUsable()) {
                logger.debug(
                    "Refresh token jti='{}' is unusable - revoked={}, used={}, expiry={}",
                    jti,
                    token.revoked,
                    token.used,
                    token.expiresAt
                )
                return false
            }
            logger.debug("Refresh token jti='{}' passed usability check", jti)
            return true
        }
        logger.debug("Refresh token jti='{}' not found in repository", jti)
        return false
    }

    fun useToken(jti: String) {
        logger.debug("Marking refresh token jti='{}' as used", jti)
        refreshTokenRepository.markAsUsedByJti(jti)
    }

    fun revokeToken(jti: String) {
        logger.debug("Revoking refresh token jti='{}'", jti)
        refreshTokenRepository.markAsRevokedByJti(jti)
    }

}