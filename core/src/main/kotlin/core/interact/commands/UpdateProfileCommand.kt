package core.interact.commands

import arrow.core.raise.Effect
import arrow.core.raise.effect
import core.BotContext
import core.assets.Channel
import core.assets.User
import core.database.repositories.ChannelProfileRepository
import core.database.repositories.UserProfileRepository
import core.interact.message.PlatformService
import core.interact.message.PublisherSet
import core.interact.reports.writeCommandReport
import core.session.entities.ChannelConfig
import utils.tuple

class UpdateProfileCommand(
    command: Command,
    private val newUser: User.Human?,
    private val newChannel: Channel?,
) : UnionCommand(command) {

    override val name = "update-profile"

    override suspend fun executeSelf(
        bot: BotContext,
        config: ChannelConfig,
        channel: Channel,
        user: User.Human,
        service: PlatformService,
        publishers: PublisherSet
    ) = runCatching {
        this.newUser?.let {
            UserProfileRepository.upsertUser(bot.dbConnection, it)
        }

        val thenUser = this.newUser ?: user

        this.newChannel?.let {
            ChannelProfileRepository.upsertChannel(bot.dbConnection, it)
        }

        val report = this.writeCommandReport("succeed", channel, user)

        val io: Effect<Nothing, Unit> = effect { }

        tuple(io, report, this.newChannel ?: channel, thenUser)
    }

}
