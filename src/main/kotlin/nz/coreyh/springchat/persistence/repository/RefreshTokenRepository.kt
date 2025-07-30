package nz.coreyh.springchat.persistence.repository

import nz.coreyh.springchat.domain.model.token.RefreshToken
import nz.coreyh.springchat.domain.model.token.Token
import nz.coreyh.springchat.persistence.table.RefreshTokenTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.springframework.stereotype.Repository

@Repository
class RefreshTokenRepository {

    fun add(token: Token) = transaction {
        RefreshTokenTable.insert {
            it[jti] = token.jti
            it[userId] = token.userId
            it[expiresAt] = token.expiresAt
            it[revoked] = false
            it[used] = false
        }
    }

    fun findByJti(jti: String): RefreshToken? = transaction {
        RefreshTokenTable.selectAll()
            .where { (RefreshTokenTable.jti eq jti) and (RefreshTokenTable.revoked eq false) }
            .map {
                RefreshToken(
                    id = it[RefreshTokenTable.id].value,
                    jti = it[RefreshTokenTable.jti],
                    userId = it[RefreshTokenTable.userId],
                    expiresAt = it[RefreshTokenTable.expiresAt],
                    revoked = it[RefreshTokenTable.revoked],
                    used = it[RefreshTokenTable.used],
                )
            }
            .firstOrNull()
    }

    fun markAsRevokedByJti(jti: String) = transaction {
        RefreshTokenTable.update({ RefreshTokenTable.jti eq jti }) {
            it[revoked] = true
        }
    }

    fun markAsUsedByJti(jti: String) = transaction {
        RefreshTokenTable.update({ RefreshTokenTable.jti eq jti }) {
            it[used] = true
        }
    }
}