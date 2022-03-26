package discord.interact.command

import core.interact.i18n.LanguageContainer
import core.interact.reports.CommandReport
import core.session.entities.GuildConfig
import discord.interact.message.DiscordMessagePublisher
import utils.monads.IO

class ParseFailure(
    val name: String,
    val comment: String,
    private val onFailure: (LanguageContainer, DiscordMessagePublisher) -> IO<Unit>
) {

    fun notice(guildConfig: GuildConfig, messagePublisher: DiscordMessagePublisher): Result<Pair<IO<Unit>, CommandReport>> =
        Result.success(onFailure(guildConfig.language.container, messagePublisher) to this.asCommandReport())

}

fun ParseFailure.asCommandReport() =
    CommandReport("PARSE-FAILURE-${this.name}", this.comment)
