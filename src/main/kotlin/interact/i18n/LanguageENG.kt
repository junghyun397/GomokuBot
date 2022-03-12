package interact.i18n

import java.util.*

open class LanguageENG : LanguageContainer {

    override fun targetRegion() = arrayOf(Locale.ENGLISH)

    override fun languageCode() = "ENG"

    override fun languageName() = "English:flag_gb:"
    override fun languageSuggestion() = "Please use the `~lang` `ENG` command."

    // # TOKENS

    override fun user() = "User"

    // ## BOARD

    override fun inProgress() = "In Progress"
    override fun finish() = "Finished"
    override fun progress() = "Turn Progress"
    override fun turns() = "Turns"
    override fun move() = "Latest Move"

    // # COMMANDS

    // ## HELP

    override fun helpCommand() = "help"
    override fun helpCommandDescription() = "Get help"

    // ## LANG

    override fun langCommand() = "lang"
    override fun langCommandDescription() = TODO("Not yet implemented")
    override fun languageCommandOptionCode() = TODO("Not yet implemented")
    override fun languageCommandOptionCodeDescription() = TODO("Not yet implemented")

    // ## STYLE

    override fun styleCommand() = "skin"
    override fun styleCommandDescription() = "Change Gomoku-board style setting used on this server."
    override fun styleCommandOptionCode() = TODO("Not yet implemented")
    override fun styleCommandOptionCodeDescription() = TODO("Not yet implemented")

    // ## RANK

    override fun rankCommand() = "rank"
    override fun rankCommandDescription() = "Show ranking from 1st to 10th"

    // ## RATING

    override fun ratingCommand() = "rating"
    override fun ratingCommandDescription() = TODO("Not yet implemented")
    override fun ratingCommandOptionUser() = TODO("Not yet implemented")
    override fun ratingCommandOptionUserDescription() = TODO("Not yet implemented")

    // ## START

    override fun startCommand() = "start"
    override fun startCommandDescription() = TODO("Not yet implemented")
    override fun startCommandOptionOpponent() = TODO("Not yet implemented")
    override fun startCommandOptionOpponentDescription() = TODO("Not yet implemented")

    // ## RESIGN

    override fun resignCommand() = "resign"
    override fun resignCommandDescription() = TODO("Not yet implemented")

    // # HELP

    // ## ABOUT

    override fun helpInfo() = "GomokuBot / Help"
    override fun helpDescription() =
        "GomokuBot is an open-source artificial intelligence Discord Bot that provides Gomoku(Omok) feature in Discord. " +
                "The collected data is used for training reinforcement learning models."
    override fun helpDeveloper() = "Developer"
    override fun helpRepository() = "Git Repository"
    override fun helpVersion() = "Version"
    override fun helpSupport() = "Support Channel"
    override fun helpInvite() = "Invite Link"

    // ## COMMAND

    override fun helpCommandInfo() = "GomokuBot / Command"
    override fun helpCommandHelp() = "`~help` Get help"
    override fun helpCommandRank() = "`~rank` Show ranking from 1st to 10th"
    override fun helpCommandLang(langList: String) =
        "`~lang` $langList Change the language setting used on this server. Ex) `~lang` `ENG`"
    override fun helpCommandSkin() =
        "`~skin` `A` `B` `C` Change the Gomoku-canvas style setting used on this server. Ex) `~skin` `A`"
    override fun helpCommandPVE() = "`~start` Start the game with A.I."
    override fun helpCommandPVP() =
        "`~start` `@mention` Start the game with the mentioned player. Ex) `~start` `@player`"
    override fun helpCommandResign() = "`~resign` Surrender the current game."

    // # STYLE

    override fun styleInformation() = "GomokuBot / Style"
    override fun styleDescription() =
        "Default Gomoku board (Style A) may not display properly. " +
                "Choose one of the three styles available and set the style to use on this server."
    override fun styleCommandInfo(style: String) = "Enter ``~skin`` ``$style`` to use this style."
    override fun styleUpdateError() = "There is an error in the style specification."
    override fun styleUpdateSuccess(style: String) = "Style setting has been change to ``$style`` !"

    // # RANK

    override fun rankInfo() = "GomokuBot / Ranking"
    override fun rankDescription() = "Ranked 1st to 10th."
    override fun rankWin() = "Victory"
    override fun rankLose() = "Defeat"

    // # RATING

    override fun ratingInfo() = TODO("Not yet implemented")
    override fun ratingDescription() = TODO("Not yet implemented")
    override fun ratingUpdate(nameTag: String, prvRating: Float, rating: Float) = TODO("Not yet implemented")
    override fun rating() = TODO("Not yet implemented")

    // # LANG

    override fun langUpdateError() = "There is an error in the language specification."
    override fun langUpdateSuccess() = "Language setting has been changed to English:flag_gb:!"

    // # GAME

    // ## CREATION

    override fun gameNotFound(nameTag: String) =
        "$nameTag, could not find any games in progress. Please start the game with `~start` command!"
    override fun gameAlreadyInProgress(nameTag: String) =
        "$nameTag, Game creation failed. Please finish the game in progress. :thinking:"
    override fun gameSyntaxFail(nameTag: String) =
        "$nameTag, that's invalid command. Please write in the format of . `~s` `alphabet` `number` :thinking:"

    // ## CREATED

    override fun gameCreatedInfo(playerName: String, targetName: String, fAttack: String) =
        "The match between`$playerName` vs `$targetName` has begun! Attack first is `$fAttack`."
    override fun gameCommandInfo() = "Please place the Stone by `~s` `alphabet` `number` format. Ex) `~s` `h` `8`"

    // ## PROCESS

    override fun gameNextTurn(curName: String, prvName: String, lastPos: String) =
        "`$curName`, please place the next Stone. `$prvName` was placed on $lastPos"
    override fun gameInvalidMoveAlreadyExits(nameTag: String) = "$nameTag, there is already a stone. :thinking:"
    override fun gameInvalidMoveForbidden(nameTag: String) = TODO("Not yet implemented")
    override fun gameTieCausedByFull() = "There was no more space for the stones, so it was a draw."

    // ## PVP

    override fun gamePVPPleaseWait(turnName: String) =
        "It's `$turnName`s turn now. Please wait for `$turnName`'s next stone. :thinking:"
    override fun gamePVPWin(winName: String, loseName: String, lastPos: String) =
        "`$winName` wins by `$loseName` placing Stone on $lastPos!"
    override fun gamePVPWinCausedByResign(winName: String, loseName: String) =
        "`$winName` wins by `$loseName` declaring surrender!"
    override fun gamePVPInfo(winName: String, loseName: String, winCount: Int, loseCount: Int) =
        "`$winName` vs `$loseName` have been updated to `$winCount : $loseCount`."

    // ## PVE

    override fun gamePVEWin(lastPos: String) =
        "You beat A.I. by placing Stone on $lastPos. Congratulations! :tada:"
    override fun gamePVELose(lastPos: String) = "You have been defeated by A.I. placing on $lastPos."
    override fun gamePVEResign() = "You have been defeated by declaring surrender."

    override fun gamePVEInfo(playerName: String, winCount: Int, loseCount: Int, rank: Int) =
        "Your entire A.I. has been updated to `$winCount : $loseCount`. $playerName is currently ranked $rank above."

}