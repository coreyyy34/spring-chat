package nz.coreyh.springchat.persistence.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

/**
 * Exposed table definition for storing chat messages.
 *
 * @property from Reference to the sender in the UserTable.
 * @property content Text content of the message, limited to 512 characters.
 * @property channelId Identifier for the chat channel the message belongs to.
 * @property timestamp UTC timestamp when the message was sent.
 */
object MessageTable : IntIdTable("message") {
    val from = reference("from", UserTable)
    val content = varchar("content", 512)
    val channelId = integer("channel")
    val timestamp = timestamp("timestamp")
}