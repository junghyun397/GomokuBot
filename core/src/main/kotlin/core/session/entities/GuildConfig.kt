package core.session.entities

import core.assets.GuildId
import core.session.ArchivePolicy
import core.session.FocusPolicy
import core.session.SweepPolicy
import core.interact.i18n.*
import core.interact.message.graphics.BoardStyle

data class GuildConfig(
    val id: GuildId,
    val language: Language = Language.ENG,
    val boardStyle: BoardStyle = BoardStyle.TEXT, // TODO
    val focusPolicy: FocusPolicy = FocusPolicy.INTELLIGENCE,
    val sweepPolicy: SweepPolicy = SweepPolicy.CLEAR_BODY,
    val archivePolicy: ArchivePolicy = ArchivePolicy.BY_ANONYMOUS
)
