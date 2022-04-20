package core.interact.i18n

enum class Language(val container: LanguageContainer) {
    ENG(LanguageENG()),
    KOR(LanguageKOR()),
    JPN(LanguageJPN()),
    VNM(LanguageVNM()),
    PRK(LanguagePRK()),
    SKO(LanguageSKO())
}

sealed interface LanguageContainer {

    fun languageCode(): String

    fun languageName(): String
    fun languageSuggestion(): String

    // # 1. INFORM

    // ## 1-1. HELP

    // ### 1-1-1. HELP (COMMAND)

    fun helpCommand(): String
    fun helpCommandDescription(): String

    // ### 1-1-2. HELP:ABOUT (EMBED)

    fun helpAboutEmbedTitle(): String
    fun helpAboutEmbedDescription(): String
    fun helpAboutEmbedDeveloper(): String
    fun helpAboutEmbedRepository(): String
    fun helpAboutEmbedVersion(): String
    fun helpAboutEmbedSupport(): String
    fun helpAboutEmbedInvite(): String

    // ### 1-1-3. HELP:COMMAND (EMBED)

    fun helpCommandEmbedTitle(): String

    fun helpCommandEmbedHelp(): String
    fun helpCommandEmbedRank(): String
    fun helpCommandEmbedRating(): String

    fun helpCommandEmbedLang(langList: String): String
    fun helpCommandEmbedStyle(): String

    fun helpCommandEmbedStartPVE(): String
    fun helpCommandEmbedStartPVP(): String
    fun helpCommandEmbedResign(): String

    // ## 1-2. RANK

    // ### 1-2-1. RANK (COMMAND)

    fun rankCommand(): String
    fun rankCommandDescription(): String

    // ### 1-2-2. RANK:LIST (EMBED)

    fun rankEmbedTitle(): String
    fun rankEmbedDescription(): String
    fun rankEmbedWin(): String
    fun rankEmbedLose(): String

    // ## 1-3. RATING

    // ### 1-3-1. RATING (COMMAND)

    fun ratingCommand(): String
    fun ratingCommandDescription(): String
    fun ratingCommandOptionUser(): String
    fun ratingCommandOptionUserDescription(): String

    // ### 1-3-2. RATING:RESPONSE (EMBED)

    fun ratingEmbed(): String
    fun ratingEmbedDescription(): String

    // # 2. CONFIG

    // ## 2-1. LANG

    // ### 2-1-1. LANG (COMMAND)

    fun languageCommand(): String
    fun languageCommandDescription(): String
    fun languageCommandOptionCode(): String
    fun languageCommandOptionCodeDescription(): String

    // ### 2-1-2. LANG:SUCCEED:UPDATED (MESSAGE)

    fun languageUpdated(): String

    // ## 2-2. STYLE

    // ### 2-2-1. STYLE (COMMAND)

    fun styleCommand(): String
    fun styleCommandDescription(): String
    fun styleCommandOptionCode(): String
    fun styleCommandOptionCodeDescription(): String

    // ### 2-2-2. STYLE:ENUM (EMBED)

    fun styleEmbedTitle(): String
    fun styleEmbedDescription(): String
    fun styleEmbedSuggestion(styleName: String): String

    // ### 2-2-3. STYLE:ERROR:NOTFOUND (MESSAGE)

    fun styleErrorNotfound(): String

    // ### 2-2-4. STYLE:SUCCESS:UPDATED (MESSAGE)

    fun styleUpdated(styleName: String): String

    // ## 2-3. POLICY

    // ### 2-3-1. POLICY (COMMAND)

    // # 3. SESSION

    fun sessionNotFound(): String

    // ## 3-1. START

    // ### 3-1-1. START (COMMAND)

    fun startCommand(): String
    fun startCommandDescription(): String
    fun startCommandOptionOpponent(): String
    fun startCommandOptionOpponentDescription(): String

    // ### 3-1-2. START:ERROR:ALREADY (MESSAGE)

    fun startErrorSessionAlready(user: String): String
    fun startErrorRequestAlready(user: String): String

    // ## 3-2. SET

    // ### 3-2-1. SET (COMMAND)

    fun setCommandDescription(): String
    fun setCommandOptionColumn(): String
    fun setCommandOptionColumnDescription(): String
    fun setCommandOptionRow(): String
    fun setCommandOptionRowDescription(): String

    // ### 3-2-2. SET:ERROR:ARGUMENT (MESSAGE)

    fun setErrorIllegalArgument(player: String): String

    // ### 3-2-3. SET:ERROR:EXIST (MESSAGE)

    fun setErrorExist(player: String, move: String): String

    // ### 3-2-4. SET:ERROR:FORBIDDEN (MESSAGE)

    fun setErrorForbidden(player: String, move: String, forbiddenKind: String): String

    // ## 3-3. RESIGN

    // ### 3-3-1. RESIGN (COMMAND)

    fun resignCommand(): String
    fun resignCommandDescription(): String

    // ## 3-4. REQUEST

    // ### 3-4-1. REQUEST:ABOUT (EMBED)

    fun requestEmbedTitle(): String
    fun requestEmbedDescription(owner: String, opponent: String): String
    fun requestEmbedButtonAccept(): String
    fun requestEmbedButtonReject(): String

    // ### 3-4-1. REQUEST:REJECTED (MESSAGE)

    fun requestRejected(owner: String, opponent: String): String

    // # 4. GAME

    // ## 4-1. BEGIN

    // ### 4-1-1. BEGIN:PVP (MESSAGE)

    fun beginPVP(owner: String, opponent: String, opener: String): String

    // ### 4-1-2. BEGIN:AI (Message)

    fun beginPVE(player: String, opener: String): String

    // ## 4-2. PROCESS

    // ### 4-2-1. PROCESS:NEXT (MESSAGE)

    fun processNext(player: String, priorPlayer: String, latestMove: String): String

    // ### 4-2-2. PROCESS:ERROR:ORDER (MESSAGE)

    fun processErrorOrder(user: String, player: String): String

    // ## 4-3. END

    // ### 4-3-1. END:PVP (MESSAGE)

    fun endPVPWin(winner: String, looser: String, latestMove: String): String
    fun endPVPResign(winner: String, looser: String): String
    fun endPVPTie(owner: String, opponent: String): String

    // ### 4-3-2. END:AI (MESSAGE)

    fun endPVEWin(player: String, latestPos: String): String
    fun endPVELose(player: String, latestPos: String): String
    fun endPVEResign(player: String): String
    fun endPVETie(player: String): String

    // # 5. BOARD

    fun boardInProgress(): String
    fun boardFinished(): String

    fun boardMoves(): String
    fun boardLatestMove(): String

    fun boardResult(): String

    fun boardWinDescription(winner: String): String
    fun boardTieDescription(): String

    fun boardCommandGuide(): String

    // # 6. UTILS

    fun somethingWrongEmbedTitle(): String

    // ## 6-1. PERMISSION-NOT-GRANTED (EMBED)

    fun permissionNotGrantedEmbedDescription(channelName: String): String
    fun permissionNotGrantedEmbedFooter(): String

    // ## 6-2. NOT-YET-IMPLEMENTED (EMBED)

    fun notYetImplementedEmbedDescription(): String
    fun notYetImplementedEmbedFooter(): String

}
