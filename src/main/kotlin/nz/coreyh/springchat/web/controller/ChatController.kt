package nz.coreyh.springchat.web.controller

import nz.coreyh.springchat.common.util.extractUser
import nz.coreyh.springchat.common.validation.MessageValidation.MESSAGE_MAX_LENGTH
import nz.coreyh.springchat.common.validation.MessageValidation.MESSAGE_MIN_LENGTH
import nz.coreyh.springchat.config.Routes
import nz.coreyh.springchat.config.Templates
import nz.coreyh.springchat.domain.model.IncomingChatMessage
import nz.coreyh.springchat.domain.model.User
import nz.coreyh.springchat.domain.model.socket.ChatMessage
import nz.coreyh.springchat.domain.service.ChannelService
import nz.coreyh.springchat.domain.service.HistoryService
import nz.coreyh.springchat.domain.service.WebSocketSessionService
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

private val logger = LoggerFactory.getLogger(ChatController::class.java)

@Controller
class ChatController(
    private val webSocketSessionService: WebSocketSessionService,
    private val historyService: HistoryService,
    private val channelService: ChannelService,
    private val messagingTemplate: SimpMessagingTemplate
) {

    @GetMapping(Routes.CHAT)
    fun getChat(
        @AuthenticationPrincipal user: User,
        model: Model
    ): String {
        logger.info("GET ${Routes.CHAT}")
        model.addAttribute("username", user.username)
        model.addAttribute("channels", channelService.getChannels())
        return Templates.CHAT
    }

    @MessageMapping("/chat")
    fun handleChatMessage(message: IncomingChatMessage, accessor: StompHeaderAccessor) {
        val user = accessor.extractUser() ?: return
        if (message.content.length !in MESSAGE_MIN_LENGTH..MESSAGE_MAX_LENGTH) {
            logger.warn("Invalid message from user ${user.username}: Length ${message.content.length}, Message ${message.content}")
            return
        }
        if (message.channelId <= 0) {
            logger.warn("Invalid message from user ${user.username}: $message")
            return
        }
        logger.info("Received message $message")
        val chatMessage = historyService.saveMessage(user, message.channelId, message.content)
        messagingTemplate.convertAndSend("/topic/channel/${message.channelId}", chatMessage)
    }

    @SubscribeMapping("/channel/{channelId}")
    fun handleChannelSubscription(channelId: String, accessor: StompHeaderAccessor): ChatMessage? {
        val user = accessor.extractUser() ?: return null
        logger.info("User ${user.username} subscribed to channel $channelId")
        return null
    }
}