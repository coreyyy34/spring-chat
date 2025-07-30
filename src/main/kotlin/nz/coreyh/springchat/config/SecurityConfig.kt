package nz.coreyh.springchat.config

import nz.coreyh.springchat.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(
    private val correlationIdFilter: CorrelationIdFilter,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
) {

    companion object {
        val AUTH_REQUIRED_MATCHERS = arrayOf(
            Routes.CHAT, "${Routes.Api.API_V1_PATH}/**", "${Routes.WEBSOCKET}/**"
        )

        val AUTH_REQUIRED_ROUTES = arrayOf(
            Routes.CHAT, Routes.Api.API_V1_PATH, Routes.WEBSOCKET
        )
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http
            .headers {
                it.frameOptions { frameOptions -> frameOptions.sameOrigin() }
            }
            .csrf {
                it.ignoringRequestMatchers("/h2-console/**", "/ws/**")
            }
            .authorizeHttpRequests {
                it
                    .requestMatchers(*AUTH_REQUIRED_MATCHERS).authenticated()
                    .anyRequest().permitAll()
            }
            .formLogin {
                it.disable()
            }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(correlationIdFilter, JwtAuthenticationFilter::class.java)
            .build()
    }

    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()
}