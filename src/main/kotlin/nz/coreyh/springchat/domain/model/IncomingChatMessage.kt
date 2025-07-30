package nz.coreyh.springchat.domain.model

data class IncomingChatMessage(
    val channelId: Int,
    val content: String,
)