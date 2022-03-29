package core.session.entities

import core.interact.i18n.*
import core.interact.message.graphics.BoardStyle

data class GuildConfig(val language: Language = Language.ENG, val boardStyle: BoardStyle = BoardStyle.IMAGE)
