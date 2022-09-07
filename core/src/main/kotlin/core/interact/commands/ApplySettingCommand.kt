package core.interact.commands

import core.BotContext
import core.assets.Guild
import core.assets.MessageRef
import core.assets.User
import core.interact.Order
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.interact.message.MessageProducer
import core.interact.message.PublisherSet
import core.interact.reports.asCommandReport
import core.session.*
import core.session.entities.GuildConfig
import utils.lang.and
import utils.structs.Identifiable
import utils.structs.map

class ApplySettingCommand(
    private val newConfig: GuildConfig,
    private val diff: Identifiable,
) : Command {

    override val name = "apply-setting"

    override val responseFlag = ResponseFlag.IMMEDIATELY

    private fun getKindNamePair(container: LanguageContainer) = when (diff) {
            is BoardStyle -> when (diff) {
                BoardStyle.IMAGE -> Pair(
                    container.style(),
                    container.styleSelectImage()
                )
                BoardStyle.TEXT -> Pair(
                    container.style(),
                    container.styleSelectText()
                )
                BoardStyle.SOLID_TEXT -> Pair(
                    container.style(),
                    container.styleSelectSolidText()
                )
                BoardStyle.UNICODE -> Pair(
                    container.style(),
                    container.styleSelectUnicodeText()
                )
            }
            is FocusPolicy -> when (diff) {
                FocusPolicy.INTELLIGENCE -> Pair(
                    container.focus(),
                    container.focusSelectIntelligence()
                )
                FocusPolicy.FALLOWING -> Pair(
                    container.focus(),
                    container.focusSelectFallowing()
                )
            }
            is SweepPolicy -> when (diff) {
                SweepPolicy.RELAY -> Pair(
                    container.sweep(),
                    container.sweepSelectRelay()
                )
                SweepPolicy.LEAVE -> Pair(
                    container.sweep(),
                    container.sweepSelectLeave()
                )
            }
            is ArchivePolicy -> when (diff) {
                ArchivePolicy.BY_ANONYMOUS -> Pair(
                    container.archive(),
                    container.archiveSelectByAnonymous()
                )
                ArchivePolicy.WITH_PROFILE -> Pair(
                    container.archive(),
                    container.archiveSelectWithProfile()
                )
                ArchivePolicy.PRIVACY -> Pair(
                    container.archive(),
                    container.archiveSelectPrivacy()
                )
            }
            else -> throw IllegalStateException()
        }

    override suspend fun <A, B> execute(
        bot: BotContext,
        config: GuildConfig,
        guild: Guild,
        user: User,
        producer: MessageProducer<A, B>,
        messageRef: MessageRef,
        publishers: PublisherSet<A, B>
    ) = runCatching {
        SessionManager.updateGuildConfig(bot.sessions, guild, newConfig)

        val (localKind, localChoice) = this.getKindNamePair(config.language.container)

        val io = producer.produceSettingApplied(publishers.windowed, config.language.container, localKind, localChoice)
            .launch()
            .map { emptyList<Order>() }

        val (kind, choice) = this.getKindNamePair(Language.ENG.container)

        io and this.asCommandReport("update $kind as [$choice](${diff.id})", guild, user)
    }

}
