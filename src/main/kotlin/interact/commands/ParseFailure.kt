package interact.commands

import interact.reports.CommandReport
import interact.reports.toCommandReport
import session.entities.GuildConfig
import utility.MessagePublisher

class ParseFailure(
    val name: String,
    val comment: String,
    private val onFailure: () -> Unit
) {

    fun notice(
        guildConfig: GuildConfig,
        messagePublisher: MessagePublisher
    ): Result<CommandReport> {
        onFailure()
        return Result.success(this.toCommandReport())
    }

}