package nz.coreyh.springchat.utils

import jakarta.servlet.http.Cookie
import nz.coreyh.springchat.service.JwtService
import nz.coreyh.springchat.domain.service.UserService
import nz.coreyh.springchat.common.util.CookieUtil.Companion.ACCESS_TOKEN_COOKIE_NAME
import org.junit.jupiter.api.assertNotNull
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.get

fun MockMvc.performAuthenticatedGet(
    path: String,
    userService: UserService,
    jwtService: JwtService,
    username: String = "testUser",
    password: String = "testPass",
): ResultActionsDsl {
    val user = userService.create(username, password)
    assertNotNull(user)

    val accessToken = jwtService.generateAccessToken(user.id)
    val authCookie = Cookie(ACCESS_TOKEN_COOKIE_NAME, accessToken)

    return this.get(path) {
        cookie(authCookie)
    }
}