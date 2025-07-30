package nz.coreyh.springchat.domain.service

import nz.coreyh.springchat.domain.model.Channel
import org.springframework.stereotype.Service

@Service
class ChannelService {

    fun getChannels(): List<Channel> {
        return listOf(
            Channel("general", 1),
            Channel("off-topic", 2),
            Channel("memes", 3),
        )
    }
}