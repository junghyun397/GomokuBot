package core.interact.message

import core.assets.*
import core.interact.i18n.LanguageContainer
import core.interact.message.graphics.HistoryRenderType
import core.session.*
import core.session.entities.GuildConfig
import utils.assets.toEnumString
import utils.lang.pair
import utils.structs.Identifiable
import utils.structs.Option
import utils.structs.toOption
import kotlin.reflect.KClass

data class SettingElement(
    val menuIndex: Int,
    val label: (LanguageContainer) -> String,
    val description: (LanguageContainer) -> String,
    val extractor: (GuildConfig) -> Any
)

data class OptionElement(
    val label: (LanguageContainer) -> String,
    val description: (LanguageContainer) -> String,
    val stringId: String,
    val shortId: Short,
    val emoji: String,
) {

    companion object {

        fun <T> fromIdentifiableEnum(enum: T, label: (LanguageContainer) -> String, description: (LanguageContainer) -> String, emoji: String)
        where T : Enum<*>, T : Identifiable =
            OptionElement(label, description, enum.toEnumString(), enum.id, emoji)

    }

}

object SettingMapping {

    val map: Map<KClass<*>, Pair<SettingElement, Map<Identifiable, OptionElement>>> = mapOf(
        BoardStyle::class to Pair(
            first = SettingElement(
                menuIndex = 1,
                label = LanguageContainer::style,
                description = LanguageContainer::styleEmbedDescription,
                extractor = { it.boardStyle },
            ),
            second = mapOf(
                BoardStyle.IMAGE to OptionElement.fromIdentifiableEnum(
                    enum = BoardStyle.IMAGE,
                    label = LanguageContainer::styleSelectImage,
                    description = LanguageContainer::styleSelectImageDescription,
                    emoji = UNICODE_IMAGE
                ),
                BoardStyle.TEXT to OptionElement.fromIdentifiableEnum(
                    enum = BoardStyle.TEXT,
                    label = LanguageContainer::styleSelectText,
                    description = LanguageContainer::styleSelectTextDescription,
                    emoji = UNICODE_T
                ),
                BoardStyle.DOTTED_TEXT to OptionElement.fromIdentifiableEnum(
                    enum = BoardStyle.DOTTED_TEXT,
                    label = LanguageContainer::styleSelectDottedText,
                    description = LanguageContainer::styleSelectDottedTextDescription,
                    emoji = UNICODE_T
                ),
                BoardStyle.UNICODE to OptionElement.fromIdentifiableEnum(
                    enum = BoardStyle.UNICODE,
                    label = LanguageContainer::styleSelectUnicodeText,
                    description = LanguageContainer::styleSelectUnicodeTextDescription,
                    emoji = UNICODE_GEM
                )
            )
        ),
        FocusPolicy::class to Pair(
            first = SettingElement(
                menuIndex = 2,
                label = LanguageContainer::focus,
                description = LanguageContainer::focusEmbedDescription,
                extractor = { it.focusPolicy },
            ),
            second = mapOf(
                FocusPolicy.INTELLIGENCE to OptionElement.fromIdentifiableEnum(
                    enum = FocusPolicy.INTELLIGENCE,
                    label = LanguageContainer::focusSelectIntelligence,
                    description = LanguageContainer::focusSelectIntelligenceDescription,
                    emoji = UNICODE_ZAP
                ),
                FocusPolicy.FALLOWING to OptionElement.fromIdentifiableEnum(
                    enum = FocusPolicy.FALLOWING,
                    label = LanguageContainer::focusSelectFallowing,
                    description = LanguageContainer::focusSelectFallowingDescription,
                    emoji = UNICODE_MAG
                )
            )
        ),
        HintPolicy::class to Pair(
            first = SettingElement(
                menuIndex = 3,
                label = LanguageContainer::hint,
                description = LanguageContainer::hintEmbedDescription,
                extractor = { it.hintPolicy },
            ),
            second = mapOf(
                HintPolicy.FIVE to OptionElement.fromIdentifiableEnum(
                    enum = HintPolicy.FIVE,
                    label = LanguageContainer::hintSelectFive,
                    description = LanguageContainer::hintSelectFiveDescription,
                    emoji = UNICODE_LIGHT
                ),
                HintPolicy.OFF to OptionElement.fromIdentifiableEnum(
                    enum = HintPolicy.OFF,
                    label = LanguageContainer::hintSelectOff,
                    description = LanguageContainer::hintSelectFiveDescription,
                    emoji = UNICODE_NOTEBOOK
                )
            )
        ),
        HistoryRenderType::class to Pair(
            first = SettingElement(
                menuIndex = 4,
                label = LanguageContainer::mark,
                description = LanguageContainer::markEmbedDescription,
                extractor = { it.markPolicy },
            ),
            second = mapOf(
                HistoryRenderType.LAST to OptionElement.fromIdentifiableEnum(
                    enum = HistoryRenderType.LAST,
                    label = LanguageContainer::markSelectLast,
                    description = LanguageContainer::markSelectLastDescription,
                    emoji = UNICODE_LINK
                ),
                HistoryRenderType.RECENT to OptionElement.fromIdentifiableEnum(
                    enum = HistoryRenderType.RECENT,
                    label = LanguageContainer::markSelectRecent,
                    description = LanguageContainer::markSelectRecentDescription,
                    emoji = UNICODE_LINKED_PAPERCLIPS
                ),
                HistoryRenderType.SEQUENCE to OptionElement.fromIdentifiableEnum(
                    enum = HistoryRenderType.SEQUENCE,
                    label = LanguageContainer::markSelectSequence,
                    description = LanguageContainer::markSelectSequenceDescription,
                    emoji = UNICODE_CHAINS
                )
            )
        ),
        SweepPolicy::class to Pair(
            first = SettingElement(
                menuIndex = 5,
                label = LanguageContainer::sweep,
                description = LanguageContainer::sweepEmbedDescription,
                extractor = { it.sweepPolicy },
            ),
            second = mapOf(
                SweepPolicy.RELAY to OptionElement.fromIdentifiableEnum(
                    enum = SweepPolicy.RELAY,
                    label = LanguageContainer::sweepSelectRelay,
                    description = LanguageContainer::sweepSelectRelayDescription,
                    emoji = UNICODE_BROOM
                ),
                SweepPolicy.LEAVE to OptionElement.fromIdentifiableEnum(
                    enum = SweepPolicy.LEAVE,
                    label = LanguageContainer::sweepSelectLeave,
                    description = LanguageContainer::sweepSelectLeaveDescription,
                    emoji = UNICODE_CABINET
                ),
                SweepPolicy.EDIT to OptionElement.fromIdentifiableEnum(
                    enum = SweepPolicy.EDIT,
                    label = LanguageContainer::sweepSelectEdit,
                    description = LanguageContainer::sweepSelectEditDescription,
                    emoji = UNICODE_RECYCLE
                )
            )
        ),
        ArchivePolicy::class to Pair(
            first = SettingElement(
                menuIndex = 6,
                label = LanguageContainer::archive,
                description = LanguageContainer::archiveEmbedDescription,
                extractor = { it.archivePolicy },
            ),
            second = mapOf(
                ArchivePolicy.BY_ANONYMOUS to OptionElement.fromIdentifiableEnum(
                    enum = ArchivePolicy.BY_ANONYMOUS,
                    label = LanguageContainer::archiveSelectByAnonymous,
                    description = LanguageContainer::archiveSelectByAnonymousDescription,
                    emoji = UNICODE_SILHOUETTE
                ),
                ArchivePolicy.WITH_PROFILE to OptionElement.fromIdentifiableEnum(
                    enum = ArchivePolicy.WITH_PROFILE,
                    label = LanguageContainer::archiveSelectWithProfile,
                    description = LanguageContainer::archiveSelectWithProfileDescription,
                    emoji = UNICODE_ID_CARD
                ),
                ArchivePolicy.PRIVACY to OptionElement.fromIdentifiableEnum(
                    enum = ArchivePolicy.PRIVACY,
                    label = LanguageContainer::archiveSelectPrivacy,
                    description = LanguageContainer::archiveSelectPrivacyDescription,
                    emoji = UNICODE_LOCK
                )
            )
        )
    )

    fun buildDifference(config: GuildConfig, kind: String, choice: String): Option<Pair<Identifiable, GuildConfig>> =
        runCatching { when (kind) {
            BoardStyle::class.simpleName -> {
                val style = BoardStyle.valueOf(choice)
                style pair config.copy(boardStyle = style)
            }
            FocusPolicy::class.simpleName -> {
                val focus = FocusPolicy.valueOf(choice)
                focus pair config.copy(focusPolicy = focus)
            }
            HintPolicy::class.simpleName -> {
                val hint = HintPolicy.valueOf(choice)
                hint pair config.copy(hintPolicy = hint)
            }
            HistoryRenderType::class.simpleName -> {
                val mark = HistoryRenderType.valueOf(choice)
                mark pair config.copy(markPolicy = mark)
            }
            SweepPolicy::class.simpleName -> {
                val sweep = SweepPolicy.valueOf(choice)
                sweep pair config.copy(sweepPolicy = sweep)
            }
            ArchivePolicy::class.simpleName -> {
                val archive = ArchivePolicy.valueOf(choice)
                archive pair config.copy(archivePolicy = archive)
            }
            else -> throw IllegalStateException()
        } }
            .toOption()

    fun buildKindNamePair(container: LanguageContainer, diff: Identifiable) = when (diff) {
        is BoardStyle -> container.style() pair when (diff) {
            BoardStyle.IMAGE -> container.styleSelectImage()
            BoardStyle.TEXT -> container.styleSelectText()
            BoardStyle.DOTTED_TEXT -> container.styleSelectDottedText()
            BoardStyle.UNICODE -> container.styleSelectUnicodeText()
        }

        is FocusPolicy -> container.focus() pair when (diff) {
            FocusPolicy.INTELLIGENCE -> container.focusSelectIntelligence()
            FocusPolicy.FALLOWING -> container.focusSelectFallowing()
        }

        is HintPolicy -> container.hint() pair when (diff) {
            HintPolicy.FIVE -> container.hintSelectFive()
            HintPolicy.OFF -> container.hintSelectOff()
        }

        is HistoryRenderType -> container.mark() pair when (diff) {
            HistoryRenderType.LAST -> container.markSelectLast()
            HistoryRenderType.RECENT -> container.markSelectRecent()
            HistoryRenderType.SEQUENCE -> container.markSelectSequence()
        }

        is SweepPolicy -> container.sweep() pair when (diff) {
            SweepPolicy.RELAY -> container.sweepSelectRelay()
            SweepPolicy.LEAVE -> container.sweepSelectLeave()
            SweepPolicy.EDIT -> container.sweepSelectEdit()
        }

        is ArchivePolicy -> container.archive() pair when (diff) {
            ArchivePolicy.BY_ANONYMOUS -> container.archiveSelectByAnonymous()
            ArchivePolicy.WITH_PROFILE -> container.archiveSelectWithProfile()
            ArchivePolicy.PRIVACY -> container.archiveSelectPrivacy()
        }

        else -> throw IllegalStateException()
    }

}
