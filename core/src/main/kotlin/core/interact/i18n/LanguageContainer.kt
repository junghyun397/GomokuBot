package core.interact.i18n

import core.engine.EngineLevel
import renju.notation.ColorContainer
import utils.Identifiable

enum class Language(override val id: Short, val container: LanguageContainer) : Identifiable {
    ENG(0, LanguageENG()),
    KOR(1, LanguageKOR()),
    JPN(2, LanguageJPN()),
    PRK(4, LanguagePRK()),
}

sealed interface LanguageContainer {

    val languageCode: String

    val languageName: String
    val languageSuggestion: String

    fun engineLevel(engine: EngineLevel): String

    val ruleSelectRenju: String
    val ruleSelectTaraguchi10: String
    val ruleSelectSoosyrv8: String

    val swapSelectYes: String
    val swapSelectNo: String

    val branchSelectSwap: String
    val branchSelectOffer: String

    val helpCommand: String
    val helpCommandDescription: String
    val helpCommandOptionShortcut: String
    val helpCommandOptionShortcutDescription: String
    val helpCommandOptionAnnouncements: String

    val settingsCommand: String
    val settingsCommandDescription: String

    val helpAboutEmbedTitle: String
    fun helpAboutEmbedDescription(platform: String): String
    val helpAboutEmbedDeveloper: String
    val helpAboutEmbedRepository: String
    val helpAboutEmbedVersion: String
    val helpAboutEmbedSupport: String
    val helpAboutEmbedInvite: String

    val commandUsageEmbedTitle: String

    val commandUsageHelp: String
    val commandUsageSettings: String
    val commandUsageRankGlobal: String
    val commandUsageRankServer: String
    val commandUsageRankUser: String
    val commandUsageReplay: String
    val commandUsageRating: String

    val commandUsageLang: String
    val commandUsageStyle: String

    val commandUsageStartEngine: String
    val commandUsageStartPvp: String
    val commandUsageResign: String

    val commandUsageBoard: String

    val replayCommand: String
    val replayCommandDescription: String

    val rankCommand: String
    val rankCommandDescription: String
    val rankCommandSubGlobal: String
    val rankCommandSubGlobalDescription: String
    val rankCommandSubServer: String
    val rankCommandSubServerDescription: String
    val rankCommandSubUser: String
    val rankCommandSubUserDescription: String
    val rankCommandOptionPlayer: String
    val rankCommandOptionPlayerDescription: String

    val rankErrorNotFound: String

    val rankEmbedTitle: String
    val rankEmbedDescription: String
    val rankEmbedWin: String
    val rankEmbedLose: String
    val rankEmbedDraw: String

    val ratingCommand: String
    val ratingCommandDescription: String
    val ratingCommandOptionUser: String
    val ratingCommandOptionUserDescription: String

    val ratingNoRecord: String

    val languageCommand: String
    val languageCommandDescription: String
    val languageCommandOptionCode: String
    val languageCommandOptionCodeDescription: String

    val languageUpdated: String

    val styleEmbedTitle: String
    val styleEmbedDescription: String
    fun styleEmbedSuggestion(styleName: String): String

    val styleErrorNotfound: String

    fun styleUpdated(styleName: String): String

    fun settingApplied(kind: String, choice: String): String

    val style: String

    val styleSelectImage: String
    val styleSelectImageDescription: String

    val styleSelectText: String
    val styleSelectTextDescription: String

    val styleSelectDottedText: String
    val styleSelectDottedTextDescription: String

    val focus: String

    val focusEmbedTitle: String
    val focusEmbedDescription: String

    val focusSelectIntelligence: String
    val focusSelectIntelligenceDescription: String

    val focusSelectCenter: String
    val focusSelectCenterDescription: String

    val hint: String

    val hintEmbedTitle: String
    val hintEmbedDescription: String

    val hintSelectFive: String
    val hintSelectFiveDescription: String

    val hintSelectOff: String
    val hintSelectOffDescription: String

    val mark: String

    val markEmbedTitle: String
    val markEmbedDescription: String

    val markSelectLast: String
    val markSelectLastDescription: String

    val markSelectRecent: String
    val markSelectRecentDescription: String

    val markSelectSequence: String
    val markSelectSequenceDescription: String

    val archive: String

    val archiveEmbedTitle: String
    val archiveEmbedDescription: String

    val archiveSelectByAnonymous: String
    val archiveSelectByAnonymousDescription: String

    val archiveSelectWithProfile: String
    val archiveSelectWithProfileDescription: String

    val archiveSelectPrivacy: String
    val archiveSelectPrivacyDescription: String

    val sessionNotFound: String

    val startCommand: String
    val startCommandDescription: String
    val startCommandOptionOpponent: String
    val startCommandOptionOpponentDescription: String
    val startCommandOptionRule: String
    val startCommandOptionRuleDescription: String

    val startErrorSessionAlready: String
    fun startErrorOpponentSessionAlready(opponent: String): String
    fun startErrorRequestAlreadySent(opponent: String): String
    fun startErrorRequestAlready(opponent: String): String
    fun startErrorOpponentRequestAlready(opponent: String): String

    val setCommandDescription: String
    val setCommandOptionPosition: String
    val setCommandOptionPositionDescription: String

    val setErrorIllegalArgument: String

    fun setErrorExist(move: String): String

    fun setErrorForbidden(move: String, forbiddenKind: String): String

    val resignCommand: String
    val resignCommandDescription: String

    val undoCommand: String
    val undoCommandDescription: String
    val undoErrorOpening: String
    val undoErrorNoMoves: String
    val undoErrorLimit: String
    fun undoCompleted(remainingUndos: Int): String
    val undoPvpCompleted: String
    val undoRequestEmbedTitle: String
    fun undoRequestEmbedDescription(requester: String, opponent: String): String
    fun undoRequestRejected(requester: String, opponent: String): String
    fun undoRequestExpired(requester: String, opponent: String): String

    val boardCommand: String
    val boardCommandDescription: String

    val requestEmbedTitle: String
    fun requestEmbedDescription(requester: String, opponent: String): String
    val requestEmbedButtonAccept: String
    val requestEmbedButtonReject: String

    fun requestRejected(requester: String, opponent: String): String

    fun requestExpired(requester: String, opponent: String): String

    val requestExpiredNewRequest: String

    fun beginPvp(players: ColorContainer<String>): String

    fun beginOpening(players: ColorContainer<String>): String

    fun beginEngineWhite(player: String, gomokubot: String): String
    fun beginEngineBlack(player: String, gomokubot: String): String

    fun processNextEngine(lastMove: String): String
    fun processNextPvp(opponent: String, lastMove: String): String
    fun processNextOpening(lastMove: String): String

    fun processErrorOrder(player: String): String

    fun gameResultFiveInRow(winner: String, loser: String): String
    fun gameResultResign(winner: String, loser: String): String
    val gameResultDraw: String
    fun gameResultTimeout(winner: String, loser: String): String

    val gameResultEngineRating: String
    val gameResultEngineRatingChange: String

    val boardInProgress: String
    val boardInOpening: String
    val boardFinished: String

    val boardMoves: String
    val boardLastMove: String

    val boardResult: String

    fun boardWinDescription(winner: String): String
    val boardTieDescription: String

    val boardCommandGuide: String
    val boardSwapGuide: String
    fun boardStatefulSwapGuide(offerCount: Int): String
    val boardBranchGuide: String
    val boardDeclareGuide: String
    val boardSelectGuide: String
    fun boardOfferGuide(remainingMoves: Int): String

    val replayEmbedWin: String
    val replayEmbedLose: String
    val replayEmbedDraw: String
    fun replayEmbedMatchInfo(totalMoves: Int): String
    val replayEmbedUnableToReplayDescription: String

    fun announceWrittenOn(date: String): String

    val somethingWrongEmbedTitle: String

    fun permissionNotGrantedEmbedDescription(channelName: String): String
    val permissionNotGrantedEmbedFooter: String

    val notYetImplementedEmbedDescription: String
    val notYetImplementedEmbedFooter: String

    val exploreAboutRenju: String

    val aboutRenjuDocument: String

}
