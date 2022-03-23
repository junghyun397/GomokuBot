package interact.commands.entities

import interact.reports.CommandReport
import route.BotContext
import session.entities.GuildConfig
import utility.MessagePublisher
import utility.UserId

sealed interface Command {

    val command: String

    suspend fun execute(
        botContext: BotContext,
        guildConfig: GuildConfig,
        userId: UserId,
        messagePublisher: MessagePublisher
    ): Result<CommandReport>

}
