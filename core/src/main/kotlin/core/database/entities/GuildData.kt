package core.database.entities

import core.assets.GuildId
import core.interact.i18n.Language
import core.interact.message.graphics.BoardStyle
import core.session.ArchivePolicy
import core.session.FocusPolicy
import core.session.SweepPolicy

data class GuildData(
    val id: GuildId,
    val language: Language,
    val boardStyle: BoardStyle,
    val focusPolicy: FocusPolicy,
    val sweepPolicy: SweepPolicy,
    val archivePolicy: ArchivePolicy
)