package nz.coreyh.springchat.domain.model.token

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

enum class TokenType(
    val typeName: String,
    val lifetime: Duration,
) {
    ACCESS("at", 2.minutes),
    REFRESH("rt", 7.days);

    companion object {
        fun fromTypeName(typeName: String): TokenType {
            return when (typeName) {
                ACCESS.typeName -> ACCESS
                REFRESH.typeName -> REFRESH
                else -> throw IllegalArgumentException("Unknown token type: $typeName")
            }
        }
    }
}