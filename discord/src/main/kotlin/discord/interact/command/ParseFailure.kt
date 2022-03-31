package discord.interact.command

import core.assets.Order
import core.interact.i18n.LanguageContainer
import core.interact.reports.CommandReport
import core.session.entities.GuildConfig
import discord.interact.message.DiscordMessagePublisher
import utils.monads.IO

class ParseFailure(
    val name: String,
    val comment: String,
    private val onFailure: (LanguageContainer, DiscordMessagePublisher) -> IO<Order>
) {

    fun notice(guildConfig: GuildConfig, messagePublisher: DiscordMessagePublisher): Result<Pair<IO<Order>, CommandReport>> =
        Result.success(onFailure(guildConfig.language.container, messagePublisher) to this.asCommandReport())

}

fun ParseFailure.asCommandReport() =
    CommandReport("PARSE-FAILURE-${this.name}", this.comment)
