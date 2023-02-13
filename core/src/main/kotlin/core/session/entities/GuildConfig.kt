package core.session.entities

import core.interact.i18n.Language
import core.interact.message.graphics.HistoryRenderType
import core.session.*

data class GuildConfig(
    val language: Language = Language.ENG,
    val boardStyle: BoardStyle = BoardStyle.IMAGE,
    val focusPolicy: FocusPolicy = FocusPolicy.INTELLIGENCE,
    val hintPolicy: HintPolicy = HintPolicy.FIVE,
    val markPolicy: HistoryRenderType = HistoryRenderType.LAST,
    val swapPolicy: SwapPolicy = SwapPolicy.RELAY,
    val archivePolicy: ArchivePolicy = ArchivePolicy.BY_ANONYMOUS
)
