package core.interact.parse

import arrow.core.raise.Effect
import core.assets.Channel
import core.assets.User
import core.interact.i18n.LanguageContainer
import core.interact.message.MessagePublisher
import core.interact.message.PlatformService
import core.interact.reports.CommandReport
import core.session.entities.ChannelConfig
import utils.tuple

class ParseFailure(
    val name: String,
    val comment: String,
    val channel: Channel,
    val user: User.Human,
    private val onFailure: (PlatformService, MessagePublisher, LanguageContainer) -> Effect<Nothing, Unit>
) {

    fun notice(config: ChannelConfig, service: PlatformService, publisher: MessagePublisher): Result<Pair<Effect<Nothing, Unit>, CommandReport>> =
        Result.success(tuple(onFailure(service, publisher, config.language.container), this.asCommandReport()))

}

fun CommandParser.asParseFailure(comment: String, channel: Channel, user: User.Human, onFailure: (PlatformService, MessagePublisher, LanguageContainer) -> Effect<Nothing, Unit>) =
    ParseFailure(this.name, comment, channel, user, onFailure)

fun ParseFailure.asCommandReport() =
    CommandReport("PARSE-FAILURE-${this.name}", this.comment, this.channel, this.user)
