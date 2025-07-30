package nz.coreyh.springchat.config

import nz.coreyh.springchat.persistence.table.MessageTable
import nz.coreyh.springchat.persistence.table.RefreshTokenTable
import nz.coreyh.springchat.persistence.table.UserTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ExposedConfig {

    @Bean
    fun createTables(): CommandLineRunner = CommandLineRunner {
        transaction {
            SchemaUtils.create(UserTable, MessageTable, RefreshTokenTable)
        }
    }
}