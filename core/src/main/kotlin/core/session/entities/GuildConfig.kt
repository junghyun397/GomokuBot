package core.session.entities

import core.interact.i18n.Language
import core.session.ArchivePolicy
import core.session.BoardStyle
import core.session.FocusPolicy
import core.session.SweepPolicy

data class GuildConfig(
    val language: Language = Language.ENG,
    val boardStyle: BoardStyle = BoardStyle.IMAGE,
    val focusPolicy: FocusPolicy = FocusPolicy.INTELLIGENCE,
    val sweepPolicy: SweepPolicy = SweepPolicy.RELAY,
    val archivePolicy: ArchivePolicy = ArchivePolicy.BY_ANONYMOUS
)
