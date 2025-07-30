package nz.coreyh.springchat.persistence.repository

import nz.coreyh.springchat.domain.model.socket.ChatMessage
import nz.coreyh.springchat.persistence.table.MessageTable
import nz.coreyh.springchat.persistence.table.UserTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository
import java.time.Instant

/**
 * Repository for accessing and manipulating message records.
 */
@Repository
class MessageRepository {

    /**
     * Retrieves the most recent messages for a specific channel.
     *
     * @param channelId ID of the channel to fetch messages from.
     * @param limit Maximum number of messages to retrieve.
     * @return List of ChatMessage objects sorted by timestamp (ascending).
     */
    fun findAll(channelId: Int, limit: Int) = transaction {
        (MessageTable innerJoin UserTable)
            .selectAll()
            .where { MessageTable.channelId eq channelId }
            .orderBy(MessageTable.timestamp, SortOrder.DESC)
            .limit(limit)
            .map {
                ChatMessage(
                    id = it[MessageTable.id].value,
                    fromId = it[MessageTable.from].value,
                    fromUsername = it[UserTable.username],
                    channelId = it[MessageTable.channelId],
                    timestamp = it[MessageTable.timestamp],
                    content = it[MessageTable.content],
                )
            }.reversed()
    }

    /**
     * Persists a new message to the database.
     *
     * @param fromId Sender's user ID.
     * @param channelId Channel where the message was sent.
     * @param timestamp UTC timestamp of the message.
     * @param content Message text.
     * @return Generated message ID.
     */
    fun save(fromId: Int, channelId: Int, timestamp: Instant, content: String): Int = transaction {
        MessageTable.insertAndGetId {
            it[MessageTable.from] = fromId
            it[MessageTable.channelId] = channelId
            it[MessageTable.timestamp] = timestamp
            it[MessageTable.content] = content
        }.value
    }
}