package nz.coreyh.springchat.domain.service

import nz.coreyh.springchat.domain.model.User
import nz.coreyh.springchat.domain.model.socket.ChatMessage
import nz.coreyh.springchat.persistence.repository.MessageRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

private val logger = LoggerFactory.getLogger(HistoryService::class.java)

@Service
class HistoryService(
    private val messageRepository: MessageRepository,
) {

    fun getMessages(channelId: Int, limit: Int = 25): List<ChatMessage> {
        return messageRepository.findAll(channelId, limit)
    }

    fun saveMessage(from: User, channelId: Int, content: String): ChatMessage {
        val timestamp = Instant.now()
        val id = messageRepository.save(from.id, channelId, timestamp, content)
        logger.info("Saved message from ${from.username} with ID $id")

        return ChatMessage(
            id = id,
            fromId = from.id,
            fromUsername = from.username,
            channelId = channelId,
            timestamp = Instant.now(),
            content = content,
        )
    }
}