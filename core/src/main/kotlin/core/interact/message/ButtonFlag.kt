package core.interact.message

enum class ButtonFlag {
    EMPTY, HIGHLIGHTED, BLACK, WHITE, BLACK_RECENT, WHITE_RECENT, FORBIDDEN, DISABLED
}

typealias InputField = List<List<Pair<String, ButtonFlag>>>
