package nz.coreyh.springchat.persistence.table

import org.jetbrains.exposed.dao.id.IntIdTable

/**
 * Exposed table definition for storing user data.
 *
 * @property username Unique username (up to 32 characters).
 * @property password Hashed password stored as a binary blob (up to 60 bytes).
 */
object UserTable : IntIdTable("user") {
    val username = varchar("username", 32)
    val password = binary("password", 60)
}