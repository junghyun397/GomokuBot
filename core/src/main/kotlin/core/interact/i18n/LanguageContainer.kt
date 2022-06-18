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

    // # 0. TOKENS

    fun aiLevelAmoeba(): String
    fun aiLevelApe(): String
    fun aiLevelBeginner(): String
    fun aiLevelModerate(): String
    fun aiLevelExpert(): String
    fun aiLevelGuru(): String

    // # 1. INFORM

    // ## 1-1. HELP

    // ### 1-1-1. HELP (COMMAND)

    fun helpCommand(): String
    fun helpCommandDescription(): String

    fun configCommand(): String
    fun configCommandDescription(): String

    // ### 1-1-2. HELP:ABOUT (EMBED)

    fun helpAboutEmbedTitle(): String
    fun helpAboutEmbedDescription(platform: String): String
    fun helpAboutEmbedDeveloper(): String
    fun helpAboutEmbedRepository(): String
    fun helpAboutEmbedVersion(): String
    fun helpAboutEmbedSupport(): String
    fun helpAboutEmbedInvite(): String

    // ### 1-1-3. HELP:COMMAND (EMBED)

    fun helpCommandEmbedTitle(): String

    fun helpCommandEmbedHelp(): String
    fun helpCommandEmbedConfig(): String
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

    fun styleErrorNotfound(user: String): String

    // ### 2-2-4. STYLE:SUCCESS:UPDATED (MESSAGE)

    fun styleUpdated(styleName: String): String

    // ## 2-3. POLICY

    fun configApplied(choice: String): String

    // ### 2-3-1. STYLE

    fun styleSelectImage(): String
    fun styleSelectImageDescription(): String

    fun styleSelectText(): String
    fun styleSelectTextDescription(): String

    fun styleSelectSolidText(): String
    fun styleSelectSolidTextDescription(): String

    fun styleSelectUnicodeText(): String
    fun styleSelectUnicodeTextDescription(): String

    // ### 2-3-2. FOCUS

    fun focusEmbedTitle(): String
    fun focusEmbedDescription(): String

    fun focusSelectIntelligence(): String
    fun focusSelectIntelligenceDescription(): String

    fun focusSelectFallowing(): String
    fun focusSelectFallowingDescription(): String

    // ### 2-3-3. SWEEP

    fun sweepEmbedTitle(): String
    fun sweepEmbedDescription(): String

    fun sweepSelectRelay(): String
    fun sweepSelectRelayDescription(): String

    fun sweepSelectLeave(): String
    fun sweepSelectLeaveDescription(): String

    // ### 2-3-4. ARCHIVE

    fun archiveEmbedTitle(): String
    fun archiveEmbedDescription(): String

    fun archiveSelectByAnonymous(): String
    fun archiveSelectByAnonymousDescription(): String

    fun archiveSelectWithProfile(): String
    fun archiveSelectWithProfileDescription(): String

    fun archiveSelectPrivacy(): String
    fun archiveSelectPrivacyDescription(): String

    // # 3. SESSION

    fun sessionNotFound(user: String): String

    // ## 3-1. START

    // ### 3-1-1. START (COMMAND)

    fun startCommand(): String
    fun startCommandDescription(): String
    fun startCommandOptionOpponent(): String
    fun startCommandOptionOpponentDescription(): String

    // ### 3-1-2. START:ERROR:ALREADY (MESSAGE)

    fun startErrorSessionAlready(user: String): String
    fun startErrorOpponentSessionAlready(owner: String, opponent: String): String
    fun startErrorRequestAlreadySent(owner: String, opponent: String): String
    fun startErrorRequestAlready(user: String, opponent: String): String
    fun startErrorOpponentRequestAlready(owner: String, opponent: String): String

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

    fun requestExpired(owner: String, opponent: String): String

    fun requestExpiredNewRequest(): String

    // # 4. GAME

    // ## 4-1. BEGIN

    // ### 4-1-1. BEGIN:PVP (MESSAGE)

    fun beginPVP(blackPlayer: String, whitePlayer: String): String

    // ### 4-1-2. BEGIN:AI (Message)

    fun beginPVEAiWhite(player: String): String

    fun beginPVEAiBlack(player: String): String

    // ## 4-2. PROCESS

    // ### 4-2-1. PROCESS:NEXT (MESSAGE)

    fun processNextPVE(owner: String, latestMove: String): String

    fun processNextPVP(player: String, priorPlayer: String, latestMove: String): String

    // ### 4-2-2. PROCESS:ERROR:ORDER (MESSAGE)

    fun processErrorOrder(user: String, player: String): String

    // ## 4-3. END

    // ### 4-3-1. END:PVP (MESSAGE)

    fun endPVPWin(winner: String, looser: String, latestMove: String): String
    fun endPVPResign(winner: String, looser: String): String
    fun endPVPTie(owner: String, opponent: String): String
    fun endPVPTimeOut(winner: String, looser: String): String

    // ### 4-3-2. END:AI (MESSAGE)

    fun endPVEWin(player: String, latestPos: String): String
    fun endPVELose(player: String, latestPos: String): String
    fun endPVEResign(player: String): String
    fun endPVETie(player: String): String
    fun endPVETimeOut(player: String): String

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
    fun notYetImplementedEmbedFooter(officialChannel: String): String

}
