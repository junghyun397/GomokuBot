package core.session.entities

import core.interact.message.graphics.*
import utils.structs.Identifiable

enum class BoardStyle(override val id: Short, val renderer: BoardRenderer, val sample: BoardRendererSample) : Identifiable {
    IMAGE(0, ImageBoardRenderer, ImageBoardRenderer),
    TEXT(1, TextBoardRenderer(), TextBoardRenderer),
    DOTTED_TEXT(2, DottedTextBoardRenderer(), DottedTextBoardRenderer),
}

enum class FocusType(override val id: Short) : Identifiable {
    INTELLIGENCE(0), CENTER(1)
}

enum class HintType(override val id: Short) : Identifiable {
    OFF(0), FIVE(1)
}

enum class SwapType(override val id: Short) : Identifiable {
    RELAY(0), ARCHIVE(1), EDIT(2)
}

enum class ArchivePolicy(override val id: Short) : Identifiable {
    WITH_PROFILE(0), BY_ANONYMOUS(1), PRIVACY(2)
}

enum class Rule(override val id: Short) : Identifiable {
    RENJU(0), RANDOM_4(1), TARAGUCHI_10(2), SOOSYRV_8(3),
    GOMOKU(10), SWAP2(11),
    FREESTYLE(20),
}
