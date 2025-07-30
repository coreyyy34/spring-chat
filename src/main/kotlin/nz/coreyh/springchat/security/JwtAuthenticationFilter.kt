package nz.coreyh.springchat.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import nz.coreyh.springchat.config.Routes
import nz.coreyh.springchat.config.SecurityConfig.Companion.AUTH_REQUIRED_ROUTES
import nz.coreyh.springchat.domain.service.UserService
import nz.coreyh.springchat.domain.service.JwtService
import nz.coreyh.springchat.common.util.CookieUtil.Companion.ACCESS_TOKEN_COOKIE_NAME
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userService: UserService,
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val path = request.requestURI

        // only run jwt filter for chat and websocket route
        // auth routes handle it themselves due to refreshing tokens
        if (AUTH_REQUIRED_ROUTES.none { path.startsWith(it) }) {
            log.debug("JWT filter skipping for path: $path")
            filterChain.doFilter(request, response)
            return
        }

        log.debug("JWT filter running for path: $path")
        val token = request.cookies?.find { it.name == ACCESS_TOKEN_COOKIE_NAME }?.value
        if (token != null) {
            logger.debug("Access token found in cookies")
            val claims = jwtService.validateAccessToken(token)
            if (claims != null) {
                log.debug("Access token is valid for userId=${claims.userId}")
                val user = userService.findById(claims.userId)
                if (user != null) {
                    log.debug("User found for userId=${user.id}, setting authentication context")
                    val authentication = UsernamePasswordAuthenticationToken(user, null, listOf())
                    SecurityContextHolder.getContext().authentication = authentication
                    filterChain.doFilter(request, response)
                    return
                } else {
                    log.debug("No user found for userId=${claims.userId}")
                }
            } else {
                log.debug("Access token is invalid or expired")
            }
        } else {
            log.debug("No access token found in cookies")
        }

        // do not redirect unauthenticated websocket requests to login
        if (path.startsWith(Routes.WEBSOCKET)) {
            log.debug("Skipping redirecting unauthenticated request from $path")
            filterChain.doFilter(request, response)
            return
        }

        // Build redirect URL for login with optional redirect back to original path
        val originalPath = request.requestURI.removePrefix("/")
        val originalQuery = request.queryString
        val redirectUrl = buildString {
            if (originalPath.isNotBlank() && !originalPath.startsWith("auth")) {
                append(originalPath)
                if (!originalQuery.isNullOrEmpty()) append("?$originalQuery")
            }
        }

        val refreshUrl = if (redirectUrl.isNotBlank()) {
            val encodedRedirectUrl = URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8.toString())
            "${Routes.Auth.LOGIN}?redirectUrl=$encodedRedirectUrl"
        } else {
            Routes.Auth.LOGIN
        }

        log.debug("Redirecting unauthenticated request from $path to $refreshUrl")
        response.sendRedirect(refreshUrl)
    }
}