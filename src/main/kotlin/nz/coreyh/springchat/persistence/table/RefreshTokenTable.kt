package nz.coreyh.springchat.persistence.table

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp

object RefreshTokenTable : IntIdTable("refresh_token") {
    val jti = varchar("jti", 36).uniqueIndex()
    val userId = integer("user").references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val expiresAt = timestamp("expires_at")
    val revoked = bool("revoked").default(false)
    val used = bool("used").default(false)
}