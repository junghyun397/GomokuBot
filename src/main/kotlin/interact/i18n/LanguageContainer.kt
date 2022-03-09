package interact.i18n

interface LanguageContainer {

    fun targetRegion(): Array<String>

    fun languageCode(): String

    fun languageName(): String
    fun languageDescription(): String

    // TOKENS

    fun user(): String

    // COMMANDS

    fun helpCommand(): String
    fun helpCommandDescription(): String

    fun langCommand(): String
    fun langCommandDescription(): String
    fun languageCommandOptionCode(): String
    fun languageCommandOptionCodeDescription(): String

    fun skinCommand(): String
    fun skinCommandDescription(): String
    fun skinCommandOptionCode(): String
    fun skinCommandOptionCodeDescription(): String

    fun rankCommand(): String
    fun rankCommandDescription(): String

    fun ratingCommand(): String
    fun ratingCommandDescription(): String
    fun ratingCommandOptionUser(): String
    fun ratingCommandOptionUserDescription(): String

    fun startCommand(): String
    fun startCommandDescription(): String
    fun startCommandOptionOpponent(): String
    fun startCommandOptionOpponentDescription(): String

    fun resignCommand(): String
    fun resignCommandDescription(): String

    // HELP

    fun helpInfo(): String
    fun helpDescription(): String
    fun helpDeveloper(): String
    fun helpRepository(): String
    fun helpVersion(): String
    fun helpSupport(): String
    fun helpInvite(): String

    fun helpCommandInfo(): String
    fun helpCommandHelp(): String
    fun helpCommandLang(langList: String): String
    fun helpCommandSkin(): String
    fun helpCommandRank(): String
    fun helpCommandPVE(): String
    fun helpCommandPVP(): String
    fun helpCommandResign(): String

    // SKIN

    fun skinInformation(): String
    fun skinDescription(): String
    fun skinCommandInfo(style: String): String
    fun skinUpdateError(): String
    fun skinUpdateSuccess(style: String): String

    // RANK

    fun rankInfo(): String
    fun rankDescription(): String
    fun rankWin(): String
    fun rankLose(): String

    // LANG
    
    fun langUpdateSuccess(): String
    fun langUpdateError(): String

    // GAME

    fun gameNotFound(nameTag: String): String
    fun gameAlreadyInProgress(nameTag: String): String
    fun gameSyntaxFail(nameTag: String): String
    fun gameAlreadyExits(nameTag: String): String

    fun gameCreatedInfo(playerName: String, targetName: String, fAttack: String): String
    fun gameCommandInfo(): String

    fun gameNextTurn(curName: String, prvName: String, lastPos: String): String
    fun gameTieCausedByFull(): String

    fun gamePVPPleaseWait(turnName: String): String
    fun gamePVPWin(winName: String, loseName: String, lastPos: String): String
    fun gamePVPWinCausedByResign(winName: String, loseName: String): String
    fun gamePVPInfo(winName: String, loseName: String, winCount: Int, loseCount: Int): String

    fun gamePVEWin(lastPos: String): String
    fun gamePVELose(lastPos: String): String
    fun gamePVEResign(): String
    fun gamePVEInfo(playerName: String, winCount: Int, loseCount: Int, rank: Int): String

    fun ratingInfo(): String
    fun ratingDescription(): String
    fun ratingUpdate(nameTag: String, prvRating: Float, rating: Float): String
    fun rating(): String

    // BOARD

    fun boardInProgress(): String
    fun boardFinish(): String
    fun boardProgress(): String
    fun boardTurns(): String
    fun boardLocation(): String

}