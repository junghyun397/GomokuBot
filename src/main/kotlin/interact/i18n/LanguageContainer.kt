package interact.i18n

interface LanguageContainer {

    fun targetRegion(): Array<String>

    fun languageCode(): String

    fun languageName(): String
    fun languageDescription(): String

    fun user(): String

    fun helpCommand(): String
    fun helpCommandDescription(): String

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

    fun skinCommand(): String
    fun skinCommandDescription(): String

    fun skinInformation(): String
    fun skinDescription(): String
    fun skinCommandInfo(style: String): String
    fun skinUpdateError(): String
    fun skinUpdateSuccess(style: String): String

    fun rankCommand(): String
    fun rankCommandDescription(): String

    fun rankInfo(): String
    fun rankDescription(): String
    fun rankWin(): String
    fun rankLose(): String
    
    fun langUpdateSuccess(): String

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

    fun langUpdateError(): String

    fun boardInProgress(): String
    fun boardFinish(): String
    fun boardProgress(): String
    fun boardTurns(): String
    fun boardLocation(): String

}