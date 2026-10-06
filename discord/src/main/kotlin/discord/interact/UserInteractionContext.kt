package discord.interact

import core.assets.Channel
import core.assets.User
import core.database.DatabaseConnection
import core.database.repositories.ChannelProfileRepository
import core.database.repositories.UserProfileRepository
import core.session.SessionManager
import core.session.entities.ChannelConfig
import discord.assets.*
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.sharding.ShardManager
import kotlin.time.Clock
import kotlin.time.Instant

data class UserInteractionContext<out E : Event>(
    override val shardManager: ShardManager,
    override val discordConfig: DiscordConfig,
    override val event: E,
    val user: User.Human,
    override val channel: Channel,
    override val config: ChannelConfig,
    override val emittedTime: Instant,
    override val source: String
) : InteractionContext<E> {

    companion object {

        context(dbConnection: DatabaseConnection)
        suspend fun <E: Event> fromJDAEvent(discordConfig: DiscordConfig, shardManager: ShardManager, event: E, jdaUser: JDAUser, jdaChannel: JDAChannel): UserInteractionContext<E> {
            val user = UserProfileRepository.retrieveOrInsertUser(DISCORD_PLATFORM_ID, jdaUser.userId()) {
                jdaUser.profile()
            }

            val channel = ChannelProfileRepository.retrieveOrInsertChannel(DISCORD_PLATFORM_ID, jdaChannel.channelId()) {
                jdaChannel.profile()
            }

            return UserInteractionContext(
                shardManager = shardManager,
                discordConfig = discordConfig,
                event = event,
                user = user,
                channel = channel,
                config = SessionManager.retrieveChannelConfig(channel),
                emittedTime = Clock.System.now(),
                source = event.abbreviation()
            )
        }

    }

}
