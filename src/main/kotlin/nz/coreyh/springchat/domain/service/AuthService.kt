package nz.coreyh.springchat.domain.service

import nz.coreyh.springchat.common.exception.InvalidCredentialsException
import nz.coreyh.springchat.common.exception.InvalidTokenException
import nz.coreyh.springchat.common.exception.ValidationException.Companion.throwValidationException
import nz.coreyh.springchat.common.validation.UserValidation.LOGIN_VALIDATION
import nz.coreyh.springchat.common.validation.UserValidation.REGISTER_VALIDATION
import nz.coreyh.springchat.common.validation.UserValidation.USERNAME_IN_USE
import nz.coreyh.springchat.domain.model.token.AuthTokens
import nz.coreyh.springchat.domain.model.User
import nz.coreyh.springchat.domain.model.dto.AuthLoginRequest
import nz.coreyh.springchat.domain.model.dto.AuthRegisterRequest
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

private val logger = LoggerFactory.getLogger(AuthService::class.java)

@Service
class AuthService(
    private val validationService: ValidationService,
    private val userService: UserService,
    private val jwtService: JwtService,
    private val refreshTokenService: RefreshTokenService,
    private val passwordEncoder: PasswordEncoder,
) {

    fun login(rawDto: AuthLoginRequest): AuthTokens {
        logger.info("Login attempt for username: ${rawDto.username}")

        val dto = rawDto.copy(username = rawDto.username.trim())
        validationService.validate(dto, LOGIN_VALIDATION)

        val user = userService.findByUsername(dto.username)
            ?.takeIf { passwordEncoder.matches(dto.password, it.password) }
            ?: throw InvalidCredentialsException()

        logger.info("Login successful for username: ${dto.username}")
        return createAuthTokens(user)
    }

    fun register(rawDto: AuthRegisterRequest): AuthTokens {
        logger.debug("Register attempt for username: ${rawDto.username}")

        val dto = rawDto.copy(username = rawDto.username.trim())
        validationService.validate(dto, REGISTER_VALIDATION)

        userService.findByUsername(dto.username)?.let {
            logger.debug("Username already in use: ${dto.username}")
            throwValidationException("username" to USERNAME_IN_USE)
        }

        val hashedPassword = passwordEncoder.encode(dto.password)
        val user = userService.create(dto.username, hashedPassword)
            ?: throw RuntimeException("Failed to create user")

        logger.debug("Registration successful for username: ${dto.username}")
        return createAuthTokens(user)
    }

    fun logout(rawRefreshToken: String?) {
        rawRefreshToken?.let {
            val claims = jwtService.validateRefreshToken(rawRefreshToken)
            if (claims != null) {
                refreshTokenService.revokeToken(claims.jti)
                logger.debug("Refresh token revoked for userId=${claims.userId}")
            }
        }
    }

    /**
     * Attempts to refresh the user's session using the provided refresh token.
     *
     * Validates the refresh token, checks if it has not been used or invalidated, and
     * issues a new access and refresh token. The old refresh token is marked as used.
     */
    fun refresh(rawRefreshToken: String?): AuthTokens {
        rawRefreshToken?.takeIf { it.isNotBlank() }?.let { token ->
            logger.debug("Attempting to validate refresh token")
            val refreshClaims = jwtService.validateRefreshToken(token)
            if (refreshClaims != null) {
                logger.debug("Refresh token is valid for userId=${refreshClaims.userId}, jti=${refreshClaims.jti}")
                if (refreshTokenService.isTokenValid(refreshClaims.jti)) {
                    logger.debug("Refresh token is active and not previously used")
                    val newRefreshToken = jwtService.generateRefreshToken(refreshClaims.userId)
                    val newAccessToken = jwtService.generateAccessToken(refreshClaims.userId)
                    refreshTokenService.useToken(refreshClaims.jti)
                    refreshTokenService.storeToken(newRefreshToken)
                    logger.debug("Issued new tokens for userId=${refreshClaims.userId}")
                    return AuthTokens(
                        accessToken = newAccessToken,
                        refreshToken = newRefreshToken
                    )
                } else {
                    logger.debug("Refresh token has already been used or is invalidated")
                }
            } else {
                logger.debug("Refresh token is invalid or expired")
            }
        } ?: logger.debug("Refresh token was not provided")
        throw InvalidTokenException()
    }

    /**
     * Attempts to authenticate a user using the provided access and refresh tokens.
     *
     * If the access token is valid, the user is considered authenticated and no new tokens are issued.
     * If the access token is invalid or expired, we attempt to refresh the session using the refresh token.
     *
     * @param rawAccessToken The raw JWT access token string, or null if not provided.
     * @param rawRefreshToken The raw JWT refresh token string, or null if not provided.
     * @return `null` if the access token is valid; otherwise, a new pair of access and refresh tokens.
     */
    fun authenticate(rawAccessToken: String?, rawRefreshToken: String?): AuthTokens? {
        logger.debug("Starting authentication attempt")

        // try to validate the access token
        rawAccessToken?.takeIf { it.isNotBlank() }?.let { token ->
            logger.debug("Attempting to validate access token")
            val accessClaims = jwtService.validateAccessToken(token)
            if (accessClaims != null) {
                logger.debug("Access token is valid for userId=${accessClaims.userId}")
                return null
            }
            logger.debug("Access token is invalid or expired")
        } ?: logger.debug("Access token was not provided")

        return refresh(rawRefreshToken)
    }

    private fun createAuthTokens(user: User): AuthTokens {
        val accessToken = jwtService.generateAccessToken(user.id)
        val refreshToken = jwtService.generateRefreshToken(user.id)
        refreshTokenService.storeToken(refreshToken)
        return AuthTokens(accessToken, refreshToken)
    }
}