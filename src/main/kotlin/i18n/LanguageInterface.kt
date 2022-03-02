package i18n

interface LanguageInterface {

    fun targetRegion(): Array<String>

    fun languageCode(): String

    fun languageName(): String
    fun languageDescription(): String

    fun helpInfo(): String
    fun helpDescription(): String
    fun helpDev(): String
    fun helpRepo(): String
    fun helpVersion(): String
    fun helpSupport(): String
    fun helpInvite(): String

    fun helpCmdInfo(): String
    fun helpCmdHelp(): String
    fun helpCmdLang(langList: String): String
    fun helpCmdSkin(): String
    fun helpCmdRank(): String
    fun helpCmdPVE(): String
    fun helpCmdPVP(): String
    fun helpCmdResign(): String

    fun skinInfo(): String
    fun skinDescription(): String
    fun skinCmdInfo(style: String): String
    fun skinUpdateError(): String
    fun skinUpdateSuccess(style: String): String

    fun rankInfo(): String
    fun rankDescription(): String
    fun rankWin(): String
    fun rankLose(): String

    fun langUpdateError(): String
    fun langUpdateSuccess(): String

    fun gameNotFound(nameTag: String): String
    fun gameCreationFail(nameTag: String): String
    fun gameSyntaxFail(nameTag: String): String
    fun gameAlreadyInProgress(nameTag: String): String

    fun gameCreateInfo(playerName: String, targetName: String, fAttack: String): String
    fun gameCmdInfo(): String

    fun gameNextTurn(curName: String, prvName: String, lastPos: String): String

    fun gamePVPNextTurn(turnName: String): String
    fun gamePVPWin(winName: String, loseName: String, lastPos: String): String
    fun gamePVPResign(winName: String, loseName: String): String
    fun gamePVPInfo(winName: String, loseName: String, winCount: Int, loseCount: Int): String

    fun gamePVEWin(lastPos: String): String
    fun gamePVELose(lastPos: String): String
    fun gamePVEResign(): String
    fun gamePVEInfo(playerName: String, winCount: Int, loseCount: Int, rank: Int): String

    fun gameFull(): String

    fun boardInProgress(): String
    fun boardFinish(): String
    fun boardProgress(): String
    fun boardTurns(): String
    fun boardLocation(): String

}