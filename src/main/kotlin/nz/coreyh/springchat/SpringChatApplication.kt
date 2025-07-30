package nz.coreyh.springchat

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
import org.springframework.boot.runApplication

// using stateless JWT authentication so do not need a user details service
@SpringBootApplication(
    exclude = [UserDetailsServiceAutoConfiguration::class]
)
class SpringChatApplication

fun main(args: Array<String>) {
    runApplication<SpringChatApplication>(*args)
}
