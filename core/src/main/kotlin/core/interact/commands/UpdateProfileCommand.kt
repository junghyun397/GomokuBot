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
import utils.lang.pair
import utils.structs.Option
import utils.structs.forEach
import utils.structs.orElseGet

class UpdateProfileCommand(
    private val command: Command,
    private val newUser: Option<User>,
    private val newGuild: Option<Guild>,
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
        this.newUser.forEach {
            UserProfileRepository.upsertUser(bot.dbConnection, it)
        }

        val thenUser = this.newUser.orElseGet { user }

        this.newGuild.forEach {
            GuildProfileRepository.upsertGuild(bot.dbConnection, it)
        }

        val thenGuild = this.newGuild.orElseGet { guild }

        this.command
            .execute(bot, config, thenGuild, thenUser, producer, messageRef, publishers)
            .map { (originalIO, originalReport) ->
                originalIO pair originalReport.copy(guild = thenGuild, user = thenUser, commandName = "$name${originalReport.commandName}")
            }
            .getOrThrow()
    }

}
