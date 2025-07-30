package nz.coreyh.springchat.persistence.repository

import nz.coreyh.springchat.domain.model.User
import nz.coreyh.springchat.persistence.table.UserTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Repository

/**
 * Repository for accessing and managing user data.
 */
@Repository
class UserRepository {

    /**
     * Finds a user by their username (case-insensitive).
     *
     * @param username The username to search for.
     * @return User object if found, or null.
     */
    fun findByUsername(username: String): User? = transaction {
        UserTable.selectAll()
            .where {
                // exposed doesn't have a lower function built in afaik
                CustomFunction("LOWER", VarCharColumnType(), UserTable.username) eq username.lowercase()
            }
            .map { toUser(it) }
            .singleOrNull()
    }

    /**
     * Finds a user by their ID.
     *
     * @param id The user's unique identifier.
     * @return User object if found, or null.
     */
    fun findById(id: Int): User? = transaction {
        UserTable.selectAll()
            .where { UserTable.id eq id }
            .map { toUser(it) }
            .singleOrNull()
    }

    /**
     * Creates a new user with the given username and password.
     *
     * @param username The new user's username.
     * @param password The new user's password (stored as bytes).
     * @return The created User object, or null if creation failed.
     */
    fun create(username: String, password: String): User? = transaction {
        UserTable.insert {
            it[UserTable.username] = username
            it[UserTable.password] = password.toByteArray(Charsets.UTF_8)
        }.resultedValues
            ?.map { toUser(it) }
            ?.firstOrNull()
    }

    /**
     * Converts a database row into a User domain object.
     *
     * @param row ResultRow from an Exposed query.
     * @return Corresponding User instance.
     */
    fun toUser(row: ResultRow) = User(
        id = row[UserTable.id].value,
        username = row[UserTable.username],
        password = row[UserTable.password].toString(Charsets.UTF_8)
    )
}