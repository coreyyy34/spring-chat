package nz.coreyh.springchat.common.util

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import nz.coreyh.springchat.domain.model.token.Token
import nz.coreyh.springchat.domain.model.token.TokenType
import org.springframework.stereotype.Component

@Component
class CookieUtil {

    companion object {
        const val ACCESS_TOKEN_COOKIE_NAME = "sc_at"
        const val REFRESH_TOKEN_COOKIE_NAME = "sc_rt"
    }

    fun getAccessTokenFromRequest(request: HttpServletRequest) =
        getTokenFromRequest(ACCESS_TOKEN_COOKIE_NAME, request)

    fun getRefreshTokenFromRequest(request: HttpServletRequest) =
        getTokenFromRequest(REFRESH_TOKEN_COOKIE_NAME, request)

    fun createAccessTokenCookie(token: nz.coreyh.springchat.domain.model.token.Token) =
        Cookie(ACCESS_TOKEN_COOKIE_NAME, token.value).apply {
            isHttpOnly = true
            secure = true
            path = "/"
            maxAge = nz.coreyh.springchat.domain.model.token.TokenType.ACCESS.lifetime.inWholeSeconds.toInt()
        }

    fun createRefreshTokenCookie(token: nz.coreyh.springchat.domain.model.token.Token) =
        Cookie(REFRESH_TOKEN_COOKIE_NAME, token.value).apply {
            isHttpOnly = true
            secure = true
            path = "/auth"
            maxAge = nz.coreyh.springchat.domain.model.token.TokenType.REFRESH.lifetime.inWholeSeconds.toInt()
        }

    fun clearAccessTokenCookie() =
        clearCookie(ACCESS_TOKEN_COOKIE_NAME)

    fun clearRefreshTokenCookie() =
        clearCookie(REFRESH_TOKEN_COOKIE_NAME)

    private fun getTokenFromRequest(name: String, request: HttpServletRequest) =
        request.cookies?.find { it.name == name }?.value

    private fun clearCookie(name: String): Cookie =
        Cookie(name, "").apply {
            isHttpOnly = true
            secure = true
            path = "/auth"
            maxAge = 0
        }
}