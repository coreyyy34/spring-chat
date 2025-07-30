package nz.coreyh.springchat.domain.service

import nz.coreyh.springchat.domain.model.User
import nz.coreyh.springchat.persistence.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun findByUsername(username: String): User? {
        return userRepository.findByUsername(username);
    }

    fun findById(id: Int): User? {
        return userRepository.findById(id);
    }

    fun create(username: String, hashedPassword: String): User? {
        return userRepository.create(username, hashedPassword);
    }
}