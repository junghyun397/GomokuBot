package core.interact.message

enum class ButtonFlag {
    FREE, HIGHLIGHTED, BLACK, WHITE, BLACK_RECENT, WHITE_RECENT, FORBIDDEN, DISABLED
}

typealias FocusedFields = List<List<Pair<String, ButtonFlag>>>
