package interact.commands

import interact.i18n.LanguageContainer
import interact.reports.CommandReport
import interact.reports.asCommandReport
import session.entities.GuildConfig
import utility.MessagePublisher

class ParseFailure(
    val name: String,
    val comment: String,
    private val onFailure: (LanguageContainer, MessagePublisher) -> Unit
) {

    fun notice(
        guildConfig: GuildConfig,
        messagePublisher: MessagePublisher
    ): Result<CommandReport> {
        onFailure(guildConfig.language.container, messagePublisher)
        return Result.success(this.asCommandReport())
    }

}
