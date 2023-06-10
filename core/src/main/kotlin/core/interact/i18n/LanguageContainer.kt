package core.interact.i18n

import utils.structs.Identifiable

enum class Language(override val id: Short, val container: LanguageContainer) : Identifiable {
    ENG(0, LanguageENG()),
    KOR(1, LanguageKOR()),
    JPN(2, LanguageJPN()),
    VNM(3, LanguageVNM()),
    PRK(4, LanguagePRK()),
    SKO(5, LanguageSKO())
}

sealed interface LanguageContainer {

    fun languageCode(): String

    fun languageName(): String
    fun languageSuggestion(): String

    fun aiLevelAmoeba(): String
    fun aiLevelApe(): String
    fun aiLevelBeginner(): String
    fun aiLevelIntermediate(): String
    fun aiLevelAdvanced(): String
    fun aiLevelExpert(): String
    fun aiLevelGuru(): String

    fun ruleSelectRenju(): String
    fun ruleSelectTaraguchi10(): String
    fun ruleSelectSoosyrv8(): String

    fun ruleDescriptionTaraguchi10(): String
    fun ruleDescriptionSoosyrv8(): String

    fun ruleReferenceTaraguchi10(): String
    fun ruleReferenceSoosyrv8(): String

    fun swapSelectYes(): String
    fun swapSelectNo(): String

    fun branchSelectSwap(): String
    fun branchSelectOffer(): String

    fun helpCommand(): String
    fun helpCommandDescription(): String
    fun helpCommandOptionShortcut(): String
    fun helpCommandOptionShortcutDescription(): String

    fun settingsCommand(): String
    fun settingsCommandDescription(): String

    fun helpAboutEmbedTitle(): String
    fun helpAboutEmbedDescription(platform: String): String
    fun helpAboutEmbedDeveloper(): String
    fun helpAboutEmbedRepository(): String
    fun helpAboutEmbedVersion(): String
    fun helpAboutEmbedSupport(): String
    fun helpAboutEmbedInvite(): String

    fun commandUsageEmbedTitle(): String

    fun commandUsageHelp(): String
    fun commandUsageSettings(): String
    fun commandUsageRankGlobal(): String
    fun commandUsageRankServer(): String
    fun commandUsageRankUser(): String
    fun commandUsageRating(): String

    fun commandUsageLang(langList: String): String
    fun commandUsageStyle(): String

    fun commandUsageStartPVE(): String
    fun commandUsageStartPVP(): String
    fun commandUsageResign(): String

    fun rankCommand(): String
    fun rankCommandDescription(): String
    fun rankCommandSubGlobal(): String
    fun rankCommandSubGlobalDescription(): String
    fun rankCommandSubServer(): String
    fun rankCommandSubServerDescription(): String
    fun rankCommandSubUser(): String
    fun rankCommandSubUserDescription(): String
    fun rankCommandOptionPlayer(): String
    fun rankCommandOptionPlayerDescription(): String

    fun rankErrorNotFound(): String

    fun rankEmbedTitle(): String
    fun rankEmbedDescription(): String
    fun rankEmbedWin(): String
    fun rankEmbedLose(): String
    fun rankEmbedDraw(): String

    fun ratingCommand(): String
    fun ratingCommandDescription(): String
    fun ratingCommandOptionUser(): String
    fun ratingCommandOptionUserDescription(): String

    fun ratingEmbed(): String
    fun ratingEmbedDescription(): String

    fun languageCommand(): String
    fun languageCommandDescription(): String
    fun languageCommandOptionCode(): String
    fun languageCommandOptionCodeDescription(): String

    fun languageUpdated(): String

    fun styleCommand(): String
    fun styleCommandDescription(): String
    fun styleCommandOptionCode(): String
    fun styleCommandOptionCodeDescription(): String

    fun styleEmbedTitle(): String
    fun styleEmbedDescription(): String
    fun styleEmbedSuggestion(styleName: String): String

    fun styleErrorNotfound(): String

    fun styleUpdated(styleName: String): String

    fun settingApplied(kind: String, choice: String): String

    fun style(): String

    fun styleSelectImage(): String
    fun styleSelectImageDescription(): String

    fun styleSelectText(): String
    fun styleSelectTextDescription(): String

    fun styleSelectDottedText(): String
    fun styleSelectDottedTextDescription(): String

    fun styleSelectUnicodeText(): String
    fun styleSelectUnicodeTextDescription(): String

    fun focus(): String

    fun focusEmbedTitle(): String
    fun focusEmbedDescription(): String

    fun focusSelectIntelligence(): String
    fun focusSelectIntelligenceDescription(): String

    fun focusSelectFallowing(): String
    fun focusSelectFallowingDescription(): String

    fun hint(): String

    fun hintEmbedTitle(): String
    fun hintEmbedDescription(): String

    fun hintSelectFive(): String
    fun hintSelectFiveDescription(): String

    fun hintSelectOff(): String
    fun hintSelectOffDescription(): String

    fun mark(): String

    fun markEmbedTitle(): String
    fun markEmbedDescription(): String

    fun markSelectLast(): String
    fun markSelectLastDescription(): String

    fun markSelectRecent(): String
    fun markSelectRecentDescription(): String

    fun markSelectSequence(): String
    fun markSelectSequenceDescription(): String

    fun swap(): String

    fun swapEmbedTitle(): String
    fun swapEmbedDescription(): String

    fun swapSelectRelay(): String
    fun swapSelectRelayDescription(): String

    fun swapSelectArchive(): String
    fun swapSelectArchiveDescription(): String

    fun swapSelectEdit(): String
    fun swapSelectEditDescription(): String

    fun archive(): String

    fun archiveEmbedTitle(): String
    fun archiveEmbedDescription(): String

    fun archiveSelectByAnonymous(): String
    fun archiveSelectByAnonymousDescription(): String

    fun archiveSelectWithProfile(): String
    fun archiveSelectWithProfileDescription(): String

    fun archiveSelectPrivacy(): String
    fun archiveSelectPrivacyDescription(): String

    fun sessionNotFound(): String

    fun startCommand(): String
    fun startCommandDescription(): String
    fun startCommandOptionOpponent(): String
    fun startCommandOptionOpponentDescription(): String
    fun startCommandOptionRule(): String
    fun startCommandOptionRuleDescription(): String

    fun startErrorSessionAlready(): String
    fun startErrorOpponentSessionAlready(opponent: String): String
    fun startErrorRequestAlreadySent(opponent: String): String
    fun startErrorRequestAlready(opponent: String): String
    fun startErrorOpponentRequestAlready(opponent: String): String

    fun setCommandDescription(): String
    fun setCommandOptionColumn(): String
    fun setCommandOptionColumnDescription(): String
    fun setCommandOptionRow(): String
    fun setCommandOptionRowDescription(): String

    fun setErrorIllegalArgument(): String

    fun setErrorExist(move: String): String

    fun setErrorForbidden(move: String, forbiddenKind: String): String

    fun resignCommand(): String
    fun resignCommandDescription(): String

    fun requestEmbedTitle(): String
    fun requestEmbedDescription(owner: String, opponent: String): String
    fun requestEmbedButtonAccept(): String
    fun requestEmbedButtonReject(): String

    fun requestRejected(owner: String, opponent: String): String

    fun requestExpired(owner: String, opponent: String): String

    fun requestExpiredNewRequest(): String

    fun beginPVP(blackPlayer: String, whitePlayer: String): String

    fun beginOpening(blackPlayer: String, whitePlayer: String): String

    fun beginPVEAiWhite(player: String): String

    fun beginPVEAiBlack(player: String): String

    fun processNextPVE(lastMove: String): String

    fun processNextPVP(priorPlayer: String, lastMove: String): String

    fun processNextOpening(lastMove: String): String

    fun processErrorOrder(player: String): String

    fun endPVPWin(winner: String, loser: String, lastMove: String): String
    fun endPVPResign(winner: String, loser: String): String
    fun endPVPTie(owner: String, opponent: String): String
    fun endPVPTimeOut(winner: String, loser: String): String

    fun endPVEWin(player: String, lastPos: String): String
    fun endPVELose(player: String, lastPos: String): String
    fun endPVEResign(player: String): String
    fun endPVETie(player: String): String
    fun endPVETimeOut(player: String): String

    fun boardInProgress(): String
    fun boardInOpening(): String
    fun boardFinished(): String

    fun boardMoves(): String
    fun boardLastMove(): String

    fun boardResult(): String

    fun boardWinDescription(winner: String): String
    fun boardTieDescription(): String

    fun boardCommandGuide(): String
    fun boardSwapGuide(): String
    fun boardStatefulSwapGuide(offerCount: Int): String
    fun boardBranchGuide(): String
    fun boardDeclareGuide(): String
    fun boardSelectGuide(): String
    fun boardOfferGuide(remainingMoves: Int): String

    fun announceWrittenOn(date: String): String

    fun somethingWrongEmbedTitle(): String

    fun permissionNotGrantedEmbedDescription(channelName: String): String
    fun permissionNotGrantedEmbedFooter(): String

    fun notYetImplementedEmbedDescription(): String
    fun notYetImplementedEmbedFooter(officialChannel: String): String

    fun exploreAboutRenju(): String

    fun aboutRenjuDocument(): String

}
