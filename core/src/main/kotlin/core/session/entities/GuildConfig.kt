package core.session.entities

import core.interact.i18n.Language
import core.interact.message.graphics.HistoryRenderType
import core.session.*

data class GuildConfig(
    val language: Language = Language.ENG,
    val boardStyle: BoardStyle = BoardStyle.IMAGE,
    val focusType: FocusType = FocusType.INTELLIGENCE,
    val hintType: HintType = HintType.FIVE,
    val markType: HistoryRenderType = HistoryRenderType.LAST,
    val swapType: SwapType = SwapType.RELAY,
    val archivePolicy: ArchivePolicy = ArchivePolicy.BY_ANONYMOUS
)
