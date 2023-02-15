package core.interact.message

import core.assets.*
import core.interact.i18n.LanguageContainer
import core.interact.message.graphics.HistoryRenderType
import core.session.*
import core.session.entities.GuildConfig
import utils.assets.toEnumString
import utils.lang.tuple
import utils.structs.*
import kotlin.reflect.KClass

data class SettingElement(
    val menuIndex: Int,
    val stringId: String,
    val label: (LanguageContainer) -> String,
    val description: (LanguageContainer) -> String,
    val extract: (GuildConfig) -> Identifiable,
    val find: (String) -> Option<Identifiable>,
    val mutate: (GuildConfig, Identifiable) -> GuildConfig
)

data class OptionElement(
    val shortId: Short,
    val stringId: String,
    val label: (LanguageContainer) -> String,
    val description: (LanguageContainer) -> String,
    val emoji: String,
) {

    companion object {

        fun <T> fromIdentifiableEnum(enum: T, label: (LanguageContainer) -> String, description: (LanguageContainer) -> String, emoji: String)
        where T : Enum<*>, T : Identifiable =
            OptionElement(enum.id, enum.toEnumString(), label, description, emoji)

    }

}

object SettingMapping {

    val map: Map<KClass<*>, Pair<SettingElement, Map<Identifiable, OptionElement>>> = mapOf(
        BoardStyle::class to Pair(
            first = SettingElement(
                menuIndex = 1,
                stringId = BoardStyle::class.simpleName!!,
                label = LanguageContainer::style,
                description = LanguageContainer::styleEmbedDescription,
                extract = { it.boardStyle },
                find = { name -> option { BoardStyle.valueOf(name) } }
            ) { config, value -> config.copy(boardStyle = value as BoardStyle) },
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
        FocusType::class to Pair(
            first = SettingElement(
                menuIndex = 2,
                stringId = FocusType::class.simpleName!!,
                label = LanguageContainer::focus,
                description = LanguageContainer::focusEmbedDescription,
                extract = { it.focusType },
                find = { name -> option { FocusType.valueOf(name) } }
            ) { config, value -> config.copy(focusType = value as FocusType) },
            second = mapOf(
                FocusType.INTELLIGENCE to OptionElement.fromIdentifiableEnum(
                    enum = FocusType.INTELLIGENCE,
                    label = LanguageContainer::focusSelectIntelligence,
                    description = LanguageContainer::focusSelectIntelligenceDescription,
                    emoji = UNICODE_ZAP
                ),
                FocusType.FALLOWING to OptionElement.fromIdentifiableEnum(
                    enum = FocusType.FALLOWING,
                    label = LanguageContainer::focusSelectFallowing,
                    description = LanguageContainer::focusSelectFallowingDescription,
                    emoji = UNICODE_MAG
                )
            )
        ),
        HintType::class to Pair(
            first = SettingElement(
                menuIndex = 3,
                stringId = HintType::class.simpleName!!,
                label = LanguageContainer::hint,
                description = LanguageContainer::hintEmbedDescription,
                extract = { it.hintType },
                find = { name -> option { HintType.valueOf(name) } }
            ) { config, value -> config.copy(hintType = value as HintType) },
            second = mapOf(
                HintType.FIVE to OptionElement.fromIdentifiableEnum(
                    enum = HintType.FIVE,
                    label = LanguageContainer::hintSelectFive,
                    description = LanguageContainer::hintSelectFiveDescription,
                    emoji = UNICODE_LIGHT
                ),
                HintType.OFF to OptionElement.fromIdentifiableEnum(
                    enum = HintType.OFF,
                    label = LanguageContainer::hintSelectOff,
                    description = LanguageContainer::hintSelectFiveDescription,
                    emoji = UNICODE_NOTEBOOK
                )
            )
        ),
        HistoryRenderType::class to Pair(
            first = SettingElement(
                menuIndex = 4,
                stringId = HistoryRenderType::class.simpleName!!,
                label = LanguageContainer::mark,
                description = LanguageContainer::markEmbedDescription,
                extract = { it.markType },
                find = { name -> option { HistoryRenderType.valueOf(name) } }
            ) { config, value -> config.copy(markType = value as HistoryRenderType) },
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
        SwapType::class to Pair(
            first = SettingElement(
                menuIndex = 5,
                stringId = SwapType::class.simpleName!!,
                label = LanguageContainer::swap,
                description = LanguageContainer::swapEmbedDescription,
                extract = { it.swapType },
                find = { name -> option { SwapType.valueOf(name) } }
            ) { config, value -> config.copy(swapType = value as SwapType) },
            second = mapOf(
                SwapType.RELAY to OptionElement.fromIdentifiableEnum(
                    enum = SwapType.RELAY,
                    label = LanguageContainer::swapSelectRelay,
                    description = LanguageContainer::swapSelectRelayDescription,
                    emoji = UNICODE_BROOM
                ),
                SwapType.ARCHIVE to OptionElement.fromIdentifiableEnum(
                    enum = SwapType.ARCHIVE,
                    label = LanguageContainer::swapSelectArchive,
                    description = LanguageContainer::swapSelectArchiveDescription,
                    emoji = UNICODE_CABINET
                ),
                SwapType.EDIT to OptionElement.fromIdentifiableEnum(
                    enum = SwapType.EDIT,
                    label = LanguageContainer::swapSelectEdit,
                    description = LanguageContainer::swapSelectEditDescription,
                    emoji = UNICODE_RECYCLE
                )
            )
        ),
        ArchivePolicy::class to Pair(
            first = SettingElement(
                menuIndex = 6,
                stringId = ArchivePolicy::class.simpleName!!,
                label = LanguageContainer::archive,
                description = LanguageContainer::archiveEmbedDescription,
                extract = { it.archivePolicy },
                find = { name -> option { ArchivePolicy.valueOf(name) } }
            ) { config, value -> config.copy(archivePolicy = value as ArchivePolicy) },
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

    fun buildKindNamePair(container: LanguageContainer, diff: Identifiable): Pair<String, String> {
        val (settingElement, map) = this.map[diff::class]!!

        return tuple(settingElement.label(container), map[diff]!!.label(container))
    }

    fun buildDifference(config: GuildConfig, kind: String, choice: String): Option<Pair<Identifiable, GuildConfig>> =
        this.map.values.firstOrNull { (settlingElement, _) -> settlingElement.stringId == kind }
            .asOption()
            .flatMap { (settingElement, _) ->
                settingElement.find(choice)
                    .map { choiceValue -> tuple(choiceValue, settingElement.mutate(config, choiceValue)) }
            }

}
