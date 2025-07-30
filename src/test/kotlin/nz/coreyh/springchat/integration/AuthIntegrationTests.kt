package nz.coreyh.springchat.integration

import nz.coreyh.springchat.config.Routes
import nz.coreyh.springchat.config.Templates
import nz.coreyh.springchat.domain.service.UserService
import nz.coreyh.springchat.domain.service.JwtService
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get


@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTests {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var jwtService: JwtService

    @Nested
    @DisplayName("AuthIntegrationTests.LoginTests")
    inner class LoginTests {

        @Test
        fun `should return login view with empty dto`() {
            mockMvc.get(Routes.Auth.LOGIN)
                .andExpect {
                    status { isOk() }
                    view { name(Templates.Auth.LOGIN) }
                    model {
                        attributeExists("dto")
                        attribute(
                            "dto", allOf<nz.coreyh.springchat.domain.model.dto.AuthLoginRequest>(
                                hasProperty("username", equalTo("")),
                                hasProperty("password", equalTo(""))
                            )
                        )
                    }
                }
        }
    }

    @Nested
    @DisplayName("AuthIntegrationTests.RegisterTests")
    inner class RegisterTests {

        @Test
        fun `should return login view with empty dto`() {
            mockMvc.get(Routes.Auth.REGISTER)
                .andExpect {
                    status { isOk() }
                    view { name(Templates.Auth.REGISTER) }
                    model {
                        attributeExists("dto")
                        attribute(
                            "dto", allOf<nz.coreyh.springchat.domain.model.dto.AuthRegisterRequest>(
                                hasProperty("username", equalTo("")),
                                hasProperty("password", equalTo("")),
                                hasProperty("confirmPassword", equalTo(""))
                            )
                        )
                    }
                }
        }
    }
}