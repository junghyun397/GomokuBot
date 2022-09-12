package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.database.repositories.GuildProfileRepository
import core.database.repositories.UserProfileRepository
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.session.entities.GuildConfig
import utils.lang.and
import utils.structs.Either
import utils.structs.fold

class UpdateProfileCommand(
    private val command: Command,
    private val newProfile: Either<User, Guild>,
) : Command {

    override val name = "update-profile+"

    override val responseFlag = this.command.responseFlag

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>
    ) = runCatching {
        val thenUser = this.newProfile.fold(
            onLeft = {
                UserProfileRepository.upsertUser(bot.dbConnection, it)
                it
            },
            onRight = { user }
        )

        val thenGuild = this.newProfile.fold(
            onLeft = { guild },
            onRight = {
                GuildProfileRepository.upsertGuild(bot.dbConnection, it)
                it
            }
        )

        this.command
            .execute(bot, config, thenGuild, thenUser, producer, messageRef, publishers)
            .map { (originalIO, originalReport) ->
                originalIO and originalReport.copy(commandName = "$name${originalReport.commandName}")
            }
            .getOrThrow()
    }

}
