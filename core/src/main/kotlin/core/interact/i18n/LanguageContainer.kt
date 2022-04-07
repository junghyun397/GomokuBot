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
    fun styleEmbedSuggestion(style: String): String

    // ### 2-2-3. STYLE:ERROR:NOTFOUND (MESSAGE)

    fun styleErrorNotfound(): String

    // ### 2-2-4. STYLE:SUCCESS:UPDATED (MESSAGE)

    fun styleUpdated(style: String): String

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

    fun startErrorSessionAlready(nameTag: String): String
    fun startErrorRequestAlready(nameTag: String): String

    // ## 3-2. SET

    // ### 3-2-1. SET (COMMAND)

    fun setCommandDescription(): String
    fun setCommandOptionRow(): String
    fun setCommandOptionRowDescription(): String
    fun setCommandOptionColumn(): String
    fun setCommandOptionColumnDescription(): String

    // ### 3-2-2. SET:ERROR:ARGUMENT (MESSAGE)

    fun setErrorIllegalArgument(): String

    // ### 3-2-3. SET:ERROR:EXIST (MESSAGE)

    fun setErrorExist(nameTag: String, pos: String): String

    // ### 3-2-4. SET:ERROR:FORBIDDEN (MESSAGE)

    fun setErrorForbidden(nameTag: String, pos: String, forbiddenType: String): String

    // ## 3-3. RESIGN

    // ### 3-3-1. RESIGN (COMMAND)

    fun resignCommand(): String
    fun resignCommandDescription(): String

    // ## 3-4. REQUEST

    // ### 3-4-1. REQUEST:ABOUT (EMBED)

    fun requestEmbedTitle(): String
    fun requestEmbedDescription(ownerName: String, opponentName: String): String
    fun requestEmbedButtonAccept(): String
    fun requestEmbedButtonReject(): String

    // # 4. GAME

    // ## 4-1. BEGIN

    // ### 4-1-1. BEGIN:PVP (MESSAGE)

    fun beginPVP(ownerName: String, opponentName: String, fMove: String): String

    // ### 4-1-2. BEGIN:AI (Message)

    fun beginPVE(playerName: String, fMove: String): String

    // ## 4-2. PROCESS

    // ### 4-2-1. PROCESS:NEXT (MESSAGE)

    fun processNext(curName: String, prvName: String, lastPos: String): String

    // ### 4-2-2. PROCESS:ERROR:ORDER (MESSAGE)

    fun processErrorOrder(turnName: String): String

    // ## 4-3. END

    // ### 4-3-1. END:PVP (MESSAGE)

    fun endPVPWin(winName: String, loseName: String, lastPos: String): String
    fun endPVPResign(winName: String, loseName: String): String
    fun endPVPTie(): String

    // ### 4-3-2. END:AI (MESSAGE)

    fun endPVEWin(latestPos: String): String
    fun endPVELose(latestPos: String): String
    fun endPVEResign(): String
    fun endPVETie(): String

    // # 5. BOARD

    fun boardInProgress(): String
    fun boardFinished(): String

    fun boardMoves(): String
    fun boardLatestMove(): String

    fun boardCommandGuide(): String

    // # 6. UTILS

    fun somethingWrongEmbedTitle(): String

    // ## 6-1. PERMISSION-NOT-GRANTED (EMBED)

    fun permissionNotGrantedEmbedDescription(channel: String): String
    fun permissionNotGrantedEmbedFooter(): String

    // ## 6-2. NOT-YET-IMPLEMENTED (EMBED)

    fun notYetImplementedEmbedDescription(): String
    fun notYetImplementedEmbedFooter(): String

}
