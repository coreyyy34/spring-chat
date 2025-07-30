package nz.coreyh.springchat.web.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import nz.coreyh.springchat.config.Routes
import nz.coreyh.springchat.config.Templates
import nz.coreyh.springchat.common.exception.InvalidCredentialsException
import nz.coreyh.springchat.common.exception.InvalidTokenException
import nz.coreyh.springchat.common.exception.ValidationException
import nz.coreyh.springchat.domain.service.AuthService
import nz.coreyh.springchat.common.util.CookieUtil
import nz.coreyh.springchat.common.util.CookieUtil.Companion.REFRESH_TOKEN_COOKIE_NAME
import nz.coreyh.springchat.domain.model.dto.AuthLoginRequest
import nz.coreyh.springchat.domain.model.dto.AuthRegisterRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

private val logger = LoggerFactory.getLogger(AuthController::class.java)

@Controller
class AuthController(
    private val authService: AuthService,
    private val cookieUtil: CookieUtil
) {

    @GetMapping(Routes.Auth.LOGIN)
    fun getLogin(
        @RequestParam(defaultValue = Routes.CHAT) redirectUrl: String,
        model: Model,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): String {
        logger.info("GET ${Routes.Auth.LOGIN}")

        return redirectIfAuthenticated(request, response, redirectUrl) {
            model.addAttribute("dto", AuthLoginRequest("", ""))
            Templates.Auth.LOGIN
        }
    }

    @PostMapping(Routes.Auth.LOGIN)
    fun postLogin(
        @ModelAttribute dto: AuthLoginRequest,
        model: Model,
        response: HttpServletResponse
    ): String {
        logger.info("POST ${Routes.Auth.LOGIN}")

        try {
            val (accessToken, refreshToken) = authService.login(dto)
            response.addCookie(cookieUtil.createAccessTokenCookie(accessToken))
            response.addCookie(cookieUtil.createRefreshTokenCookie(refreshToken))
            return "redirect:${Routes.CHAT}"
        } catch (exception: ValidationException) {
            model.addAttribute("errors", exception.errors)
        } catch (exception: InvalidCredentialsException) {
            model.addAttribute("globalError", "Invalid username or password")
        }
        model.addAttribute("dto", dto.copy(password = ""))
        return Templates.Auth.LOGIN
    }

    @GetMapping(Routes.Auth.REGISTER)
    fun getRegister(
        @RequestParam(defaultValue = Routes.CHAT) redirectUrl: String,
        model: Model,
        request: HttpServletRequest,
        response: HttpServletResponse
    ): String {
        logger.info("GET ${Routes.Auth.REGISTER}")

        return redirectIfAuthenticated(request, response, redirectUrl) {
            model.addAttribute("dto", AuthRegisterRequest("", "", ""))
            Templates.Auth.REGISTER
        }
    }

    @PostMapping(Routes.Auth.REGISTER)
    fun postRegister(
        @ModelAttribute dto: AuthRegisterRequest,
        model: Model,
        response: HttpServletResponse
    ): String {
        logger.info("POST ${Routes.Auth.REGISTER}")

        try {
            val (accessToken, refreshToken) = authService.register(dto)
            response.addCookie(cookieUtil.createAccessTokenCookie(accessToken))
            response.addCookie(cookieUtil.createRefreshTokenCookie(refreshToken))
            return "redirect:${Routes.CHAT}"
        } catch (exception: ValidationException) {
            model.addAttribute("dto", dto.copy(password = ""))
            model.addAttribute("errors", exception.errors)
        }
        return Templates.Auth.REGISTER
    }

    @GetMapping(Routes.Auth.LOGOUT)
    fun getLogout(
        @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) refreshToken: String?,
        response: HttpServletResponse
    ): String {
        logger.info("GET ${Routes.Auth.LOGOUT}")

        response.addCookie(cookieUtil.clearAccessTokenCookie())
        response.addCookie(cookieUtil.clearRefreshTokenCookie())
        return "redirect:${Routes.Auth.LOGIN}"
    }

    private fun redirectIfAuthenticated(
        request: HttpServletRequest,
        response: HttpServletResponse,
        redirectUrl: String,
        onInvalidTokens: () -> String
    ): String {
        val accessToken = cookieUtil.getAccessTokenFromRequest(request)
        val refreshToken = cookieUtil.getRefreshTokenFromRequest(request)

        return try {
            authService.authenticate(accessToken, refreshToken)?.let {
                val accessTokenCookie = cookieUtil.createAccessTokenCookie(it.accessToken)
                val refreshTokenCookie = cookieUtil.createRefreshTokenCookie(it.refreshToken)
                response.addCookie(accessTokenCookie)
                response.addCookie(refreshTokenCookie)
            }
            if (!redirectUrl.startsWith("auth"))
                return "redirect:/${redirectUrl}"
            return "redirect:${Routes.CHAT}"
        } catch (e: InvalidTokenException) {
            // clear them in case they are expired/invalid so we don't have to decode them next time
            response.addCookie(cookieUtil.clearAccessTokenCookie())
            response.addCookie(cookieUtil.clearRefreshTokenCookie())
            onInvalidTokens()
        }
    }
}