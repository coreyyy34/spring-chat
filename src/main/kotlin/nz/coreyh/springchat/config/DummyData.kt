package nz.coreyh.springchat.config

import nz.coreyh.springchat.persistence.repository.MessageRepository
import nz.coreyh.springchat.persistence.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.random.Random

private val log = LoggerFactory.getLogger(DummyData::class.java)

@Component
class DummyData(
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository
) {

    val usernames = listOf("alice", "bob", "charlie", "dave", "eve", "frank", "grace", "heidi", "ivan", "judy")
    val sampleMessages = listOf(
        // Short messages
        "Hey, you free this weekend?",
        "Just got coffee, anyone want some?",
        "Lol that was wild 😂",
        "Running late, be there in 10.",
        "What's the plan for tonight?",
        "Yo, check this out! 😎",
        "I'm starving, let's grab food.",
        "Movie night? 🍿",
        "Ugh, traffic is the worst.",
        "Happy birthday! 🎉",
        // Medium-length messages
        "Just saw the new art exhibit downtown, it's actually pretty cool. Anyone else been?",
        "I'm thinking of trying that new ramen place. Heard good things, but is it worth the hype?",
        "Forgot my umbrella and got soaked on the way home. Typical Monday 😅",
        "Anyone up for a quick hike this weekend? Thinking about the trail by the lake.",
        "Just finished a book that was way better than I expected. Anyone want the title?",
        "That concert last night was unreal! Still buzzing from it 🎶",
        "Need recs for a good podcast to listen to on my commute. Any faves?",
        "My dog just stole my sandwich off the table. Send help 🐶",
        "Thinking of painting my room this weekend. Any color suggestions?",
        "Just booked a trip for next month! So excited to get away for a bit ✈️",
        // Long messages (up to 512 characters)
        "Okay, so I just got back from the farmers market, and they had the best fresh produce I've seen in ages. Picked up some heirloom tomatoes, fresh basil, and this amazing homemade sourdough. Thinking of making a big caprese salad for dinner tonight. Anyone want to join? I might even throw in some grilled peaches for dessert if I’m feeling fancy. Let me know if you’re around! 😋",
        "I went to that new arcade bar last night, and it was such a vibe. They’ve got all the classic games like pinball and Pac-Man, plus some newer ones. The drinks were decent, but the atmosphere was the real winner. Took me back to being a kid at the arcade with a pocket full of quarters. Anyone down to check it out next week? I’m already itching to go back and beat my high score! 🎮",
        "So, I’ve been trying to get into running lately, and it’s harder than I thought. Did a 5k loop this morning, and my legs are screaming. Any tips for sticking with it? I’m thinking of signing up for a half-marathon in a few months to keep myself motivated, but I’m not sure if I’m ready. Also, does anyone know a good stretching routine to avoid feeling like a rusty robot after every run? 🏃‍♂️",
        "Just spent the whole day reorganizing my place, and I’m exhausted but it feels so good. Found a box of old photos from like 10 years ago, and now I’m all nostalgic. Thinking of scanning them to share with the group—some of you are in them! Also, I’m finally getting rid of that ugly lamp I’ve been meaning to toss for years. Anyone need furniture or decor? I’ve got some stuff I’m giving away. 🛋️",
        "I tried making homemade pizza last night, and it was a total adventure. The dough was stickier than I expected, and I definitely overdid it with the toppings, but it still turned out pretty tasty. Next time, I’m going for a classic margherita to keep it simple. Anyone got a go-to pizza recipe or tips for getting the crust just right? I’m determined to master this. 🍕"
    )

    @Bean
    fun createDummyData(): CommandLineRunner = CommandLineRunner {
        if (userRepository.findByUsername("alice") != null) {
            log.warn("Skipping creating dummy data, user with name 'alice' was found.")
            return@CommandLineRunner
        }

        val users = usernames.mapNotNull { username ->
            userRepository.create(username, "password123")
        }
        log.info("Seeded ${users.size} users.")

        val totalMessages = 500
        var messagesGenerated = 0
        val days = 30
        val baseTimestamp = Instant.now().truncatedTo(ChronoUnit.DAYS) // Start of today

        for (day in 0 until days) {
            val messagesThisDay = Random.nextInt(totalMessages / days)
            var dailyMessages = 0

            while (dailyMessages < messagesThisDay && messagesGenerated < totalMessages) {
                val user = users.random()
                val channelId = 3;
                val burstSize = Random.nextInt(2, 6) // 2 to 5 consecutive messages
                val dayOffsetSeconds = (days - 1 - day) * 24L * 60 * 60 // Days back from today
                val burstStartSeconds = Random.nextLong(0, 60L * 60 * 24) // Random time within the day
                val burstDuration = Random.nextLong(60, 900) // 1 to 15 minutes for the burst

                repeat(burstSize) { index ->
                    if (dailyMessages >= messagesThisDay || messagesGenerated >= totalMessages) return@repeat
                    val offsetSeconds = dayOffsetSeconds + burstStartSeconds + (index * burstDuration / burstSize)
                    val timestamp = baseTimestamp.minusSeconds(offsetSeconds)
                    val message = sampleMessages.random()
                    messageRepository.save(user.id, channelId, timestamp, message)
                    dailyMessages++
                    messagesGenerated++
                }
            }
        }

        log.info("Seeded $totalMessages messages across channels 1, 2, and 3 over the past $days days.")
    }
}