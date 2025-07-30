package nz.coreyh.springchat.common.util

import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.*

@Component
class SecurityUtil {
    private val sha256Digest: MessageDigest = MessageDigest.getInstance("MD5")

    fun sha256(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val hash = sha256Digest.digest(bytes)
        return Base64.getEncoder().encodeToString(hash)
    }
}