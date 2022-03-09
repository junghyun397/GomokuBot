package interact.i18n

open class LanguageENG : LanguageContainer {

    override fun targetRegion(): Array<String> = arrayOf()

    override fun languageCode(): String = "ENG"

    override fun languageName(): String = "English:flag_gb:"
    override fun languageDescription(): String = "Please use the `~lang` `ENG` command."

    // TOKENS

    override fun user(): String = "User"

    // COMMANDS

    override fun helpCommand(): String = "help"
    override fun helpCommandDescription(): String = "Get help"

    override fun langCommand(): String = TODO("Not yet implemented")
    override fun langCommandDescription(): String = TODO("Not yet implemented")
    override fun languageCommandOptionCode(): String = TODO("Not yet implemented")
    override fun languageCommandOptionCodeDescription(): String = TODO("Not yet implemented")

    override fun skinCommand(): String = "skin"
    override fun skinCommandDescription(): String = "Change Gomoku-board style setting used on this server."
    override fun skinCommandOptionCode(): String = TODO("Not yet implemented")
    override fun skinCommandOptionCodeDescription(): String = TODO("Not yet implemented")

    override fun rankCommand(): String = "rank"
    override fun rankCommandDescription(): String = "Show ranking from 1st to 10th"

    override fun ratingCommand(): String = TODO("Not yet implemented")
    override fun ratingCommandDescription(): String = TODO("Not yet implemented")
    override fun ratingCommandOptionUser(): String = TODO("Not yet implemented")
    override fun ratingCommandOptionUserDescription(): String = TODO("Not yet implemented")

    override fun startCommand(): String = "start"
    override fun startCommandDescription(): String = TODO("Not yet implemented")
    override fun startCommandOptionOpponent(): String = TODO("Not yet implemented")
    override fun startCommandOptionOpponentDescription(): String = TODO("Not yet implemented")

    override fun resignCommand(): String = TODO("Not yet implemented")
    override fun resignCommandDescription(): String = TODO("Not yet implemented")

    // HELP

    override fun helpInfo(): String = "GomokuBot / Help"
    override fun helpDescription(): String =
        "GomokuBot is an open-source artificial intelligence Discord Bot that provides Gomoku(Omok) feature in Discord. " +
                "The collected data is used for training reinforcement learning models."
    override fun helpDeveloper(): String = "Developer"
    override fun helpRepository(): String = "Git Repository"
    override fun helpVersion(): String = "Version"
    override fun helpSupport(): String = "Support Channel"
    override fun helpInvite(): String = "Invite Link"

    override fun helpCommandInfo(): String = "GomokuBot / Command"
    override fun helpCommandHelp(): String = "`~help` Get help"
    override fun helpCommandRank(): String = "`~rank` Show ranking from 1st to 10th"
    override fun helpCommandLang(langList: String): String =
        "`~lang` $langList Change the language setting used on this server. Ex) `~lang` `ENG`"
    override fun helpCommandSkin(): String =
        "`~skin` `A` `B` `C` Change the Gomoku-canvas style setting used on this server. Ex) `~skin` `A`"
    override fun helpCommandPVE(): String = "`~start` Start the game with A.I."
    override fun helpCommandPVP(): String =
        "`~start` `@mention` Start the game with the mentioned player. Ex) `~start` `@player`"
    override fun helpCommandResign(): String = "`~resign` Surrender the current game."

    override fun skinInformation(): String = "GomokuBot / Style"
    override fun skinDescription(): String =
        "The default Gomoku canvas (Style A) may not display properly. " +
                "Choose one of the three styles available and set the style to use on this server."
    override fun skinCommandInfo(style: String): String = "Enter ``~skin`` ``$style`` to use this style."
    override fun skinUpdateError(): String = "There is an error in the style specification."
    override fun skinUpdateSuccess(style: String): String = "Style setting has been change to ``$style`` !"

    override fun rankInfo(): String = "GomokuBot / Ranking"
    override fun rankDescription(): String = "Ranked 1st to 10th."
    override fun rankWin(): String = "Victory"
    override fun rankLose(): String = "Defeat"

    override fun ratingInfo(): String = TODO("Not yet implemented")
    override fun ratingDescription(): String = TODO("Not yet implemented")
    override fun ratingUpdate(nameTag: String, prvRating: Float, rating: Float): String = TODO("Not yet implemented")
    override fun rating(): String = TODO("Not yet implemented")

    override fun langUpdateError(): String = "There is an error in the language specification."
    override fun langUpdateSuccess(): String = "Language setting has been changed to English:flag_gb:!"

    override fun gameNotFound(nameTag: String): String =
        "$nameTag, could not find any games in progress. Please start the game with `~start` command!"
    override fun gameAlreadyInProgress(nameTag: String): String =
        "$nameTag, Game creation failed. Please finish the game in progress. :thinking:"
    override fun gameSyntaxFail(nameTag: String): String =
        "$nameTag, that's invalid command. Please write in the format of . `~s` `alphabet` `number` :thinking:"
    override fun gameAlreadyExits(nameTag: String): String = "$nameTag, there is already a stone. :thinking:"

    override fun gameCreatedInfo(playerName: String, targetName: String, fAttack: String): String =
        "The match between`$playerName` vs `$targetName` has begun! Attack first is `$fAttack`."
    override fun gameCommandInfo(): String = "Please place the Stone by `~s` `alphabet` `number` format. Ex) `~s` `h` `8`"

    override fun gameNextTurn(curName: String, prvName: String, lastPos: String): String =
        "`$curName`, please place the next Stone. `$prvName` was placed on $lastPos"

    override fun gamePVPPleaseWait(turnName: String): String =
        "It's `$turnName`s turn now. Please wait for `$turnName`'s next stone. :thinking:"
    override fun gamePVPWin(winName: String, loseName: String, lastPos: String): String =
        "`$winName` wins by `$loseName` placing Stone on $lastPos!"
    override fun gamePVPWinCausedByResign(winName: String, loseName: String): String =
        "`$winName` wins by `$loseName` declaring surrender!"

    override fun gamePVPInfo(winName: String, loseName: String, winCount: Int, loseCount: Int): String =
        "`$winName` vs `$loseName` have been updated to `$winCount : $loseCount`."

    override fun gamePVEWin(lastPos: String): String =
        "You beat A.I. by placing Stone on $lastPos. Congratulations! :tada:"
    override fun gamePVELose(lastPos: String): String = "You have been defeated by A.I. placing on $lastPos."
    override fun gamePVEResign(): String = "You have been defeated by declaring surrender."

    override fun gamePVEInfo(playerName: String, winCount: Int, loseCount: Int, rank: Int): String =
        "Your entire A.I. has been updated to `$winCount : $loseCount`. $playerName is currently ranked $rank above."

    override fun gameTieCausedByFull(): String = "There was no more space for the stones, so it was a draw."

    override fun boardInProgress(): String = "in Processing"
    override fun boardFinish(): String = "Finished"
    override fun boardProgress(): String = "Turn Progress"
    override fun boardTurns(): String = "Turns"
    override fun boardLocation(): String = "Latest Location"

}
