package nz.coreyh.springchat.domain.model.socket

import java.time.Instant

data class ChatMessage(
    val id: Int,
    val fromId: Int,
    val fromUsername: String?,
    val channelId: Int,
    val timestamp: Instant,
    val content: String,
)