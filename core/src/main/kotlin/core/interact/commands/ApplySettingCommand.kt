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

    override val responseFlag = ResponseFlag.Immediately

    private fun getKindNamePair(container: LanguageContainer) = when (diff) {
        is BoardStyle -> container.style() and when (diff) {
            BoardStyle.IMAGE -> container.styleSelectImage()
            BoardStyle.TEXT -> container.styleSelectText()
            BoardStyle.SOLID_TEXT -> container.styleSelectSolidText()
            BoardStyle.UNICODE -> container.styleSelectUnicodeText()
        }

        is FocusPolicy -> container.focus() and when (diff) {
            FocusPolicy.INTELLIGENCE -> container.focusSelectIntelligence()
            FocusPolicy.FALLOWING -> container.focusSelectFallowing()
        }

        is HintPolicy -> container.hint() and when (diff) {
            HintPolicy.FIVE -> container.hintSelectFive()
            HintPolicy.OFF -> container.hintSelectOff()
        }

        is SweepPolicy -> container.sweep() and when (diff) {
            SweepPolicy.RELAY -> container.sweepSelectRelay()
            SweepPolicy.LEAVE -> container.sweepSelectLeave()
            SweepPolicy.EDIT -> container.sweepSelectEdit()
        }

        is ArchivePolicy -> container.archive() and when (diff) {
            ArchivePolicy.BY_ANONYMOUS -> container.archiveSelectByAnonymous()
            ArchivePolicy.WITH_PROFILE -> container.archiveSelectWithProfile()
            ArchivePolicy.PRIVACY -> container.archiveSelectPrivacy()
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
