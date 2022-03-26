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

    // # TOKENS

    fun user(): String

    // ## BOARD

    fun inProgress(): String
    fun finish(): String
    fun progress(): String
    fun turns(): String
    fun move(): String

    // # COMMANDS

    // ## HELP

    fun helpCommand(): String
    fun helpCommandDescription(): String

    // ## LANG

    fun langCommand(): String
    fun langCommandDescription(): String
    fun languageCommandOptionCode(): String
    fun languageCommandOptionCodeDescription(): String

    // ## STYLE

    fun styleCommand(): String
    fun styleCommandDescription(): String
    fun styleCommandOptionCode(): String
    fun styleCommandOptionCodeDescription(): String

    // ## RANK

    fun rankCommand(): String
    fun rankCommandDescription(): String

    // ## RATING

    fun ratingCommand(): String
    fun ratingCommandDescription(): String
    fun ratingCommandOptionUser(): String
    fun ratingCommandOptionUserDescription(): String

    // ## START

    fun startCommand(): String
    fun startCommandDescription(): String
    fun startCommandOptionOpponent(): String
    fun startCommandOptionOpponentDescription(): String

    // ## RESIGN

    fun resignCommand(): String
    fun resignCommandDescription(): String

    // # HELP

    // ## ABOUT - EMBED

    fun helpAboutTitle(): String
    fun helpAboutDescription(): String
    fun helpAboutDeveloper(): String
    fun helpAboutRepository(): String
    fun helpAboutVersion(): String
    fun helpAboutSupport(): String
    fun helpAboutInvite(): String

    // ## COMMAND - EMBED

    fun helpCommandInfo(): String
    fun helpCommandHelp(): String
    fun helpCommandLang(langList: String): String
    fun helpCommandStyle(): String
    fun helpCommandRank(): String
    fun helpCommandRating(): String
    fun helpCommandPVE(): String
    fun helpCommandPVP(): String
    fun helpCommandResign(): String

    // # STYLE

    // ## EMBED

    fun styleInfo(): String
    fun styleDescription(): String
    fun styleSuggestion(style: String): String

    // ## MESSAGE

    fun styleUpdateError(): String
    fun styleUpdateSuccess(style: String): String

    // # LANG - MESSAGE

    fun langUpdateError(): String
    fun langUpdateSuccess(): String

    // # RANK - EMBED

    fun rankInfo(): String
    fun rankDescription(): String
    fun rankWin(): String
    fun rankLose(): String

    // # RATING - EMBED

    fun ratingInfo(): String
    fun ratingDescription(): String
    fun ratingUpdate(nameTag: String, prvRating: Float, rating: Float): String
    fun rating(): String

    // # GAME

    // ## CREATION

    fun gameNotFound(nameTag: String): String
    fun gameAlreadyInProgress(nameTag: String): String
    fun gameSyntaxFail(nameTag: String): String

    // ## CREATED

    fun gameCreatedInfo(playerName: String, targetName: String, fAttack: String): String
    fun gameCommandInfo(): String

    // ## PROCESS

    fun gameNextTurn(curName: String, prvName: String, lastPos: String): String
    fun gameInvalidMoveAlreadyExits(nameTag: String): String
    fun gameInvalidMoveForbidden(nameTag: String): String
    fun gameTieCausedByFull(): String

    // ## PVP

    fun gamePVPPleaseWait(turnName: String): String
    fun gamePVPWin(winName: String, loseName: String, lastPos: String): String
    fun gamePVPWinCausedByResign(winName: String, loseName: String): String
    fun gamePVPInfo(winName: String, loseName: String, winCount: Int, loseCount: Int): String

    // ## PVE

    fun gamePVEWin(lastPos: String): String
    fun gamePVELose(lastPos: String): String
    fun gamePVEResign(): String
    fun gamePVEInfo(playerName: String, winCount: Int, loseCount: Int, rank: Int): String

}
