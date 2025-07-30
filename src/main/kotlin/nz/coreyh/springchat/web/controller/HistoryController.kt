package nz.coreyh.springchat.web.controller

import nz.coreyh.springchat.config.Routes
import nz.coreyh.springchat.domain.model.socket.ChatMessage
import nz.coreyh.springchat.domain.service.HistoryService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

private val logger = LoggerFactory.getLogger(HistoryController::class.java)

@RestController
class HistoryController(
    private val historyService: HistoryService
) {

    @GetMapping(Routes.Api.History.CHANNEL)
    fun getHistory(@PathVariable channelId: Int): List<ChatMessage> {
        logger.info("GET ${Routes.Api.History.CHANNEL} - channelId: $channelId")
        return historyService.getMessages(channelId)
    }
}