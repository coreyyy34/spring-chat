package nz.coreyh.springchat.config

import nz.coreyh.springchat.security.socket.ChannelInterceptor
import nz.coreyh.springchat.security.socket.HandshakeInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val handshakeInterceptor: HandshakeInterceptor,
    private val channelInterceptor: ChannelInterceptor
) : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        // endpoints that can be subscribed to
        config.enableSimpleBroker("/topic", "/queue")

        // messages received with prefix /app will be routed to controllers @MessageMapping method.
        config.setApplicationDestinationPrefixes("/app")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // handshake endpoint
        registry.addEndpoint("/ws")
            .addInterceptors(handshakeInterceptor)
            .setAllowedOrigins("*")
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(channelInterceptor)
    }
}