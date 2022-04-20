package core.interact.i18n

open class LanguageENG : LanguageContainer {

    override fun languageCode() = "ENG"

    override fun languageName() = "English\uD83C\uDDEC\uD83C\uDDE7"
    override fun languageSuggestion() = "Please use the `~lang` `ENG` command."

    // # 1. INFORM

    // ## 1-1. HELP

    // ### 1-1-1. HELP (COMMAND)

    override fun helpCommand() = "help"
    override fun helpCommandDescription() = "Get help"

    // ### 1-1-2. HELP:ABOUT (EMBED)

    override fun helpAboutEmbedTitle() = "GomokuBot / Help"
    override fun helpAboutEmbedDescription() =
        "GomokuBot is an open-source artificial intelligence Discord Bot that provides Renju(Gomoku) feature in Discord. " +
                "The collected data is used for training reinforcement learning models."
    override fun helpAboutEmbedDeveloper() = "Developer"
    override fun helpAboutEmbedRepository() = "Git Repository"
    override fun helpAboutEmbedVersion() = "Version"
    override fun helpAboutEmbedSupport() = "Support Channel"
    override fun helpAboutEmbedInvite() = "Invite Link"

    // ### 1-1-3. HELP:COMMAND (EMBED)

    override fun helpCommandEmbedTitle() = "GomokuBot / Command"
    override fun helpCommandEmbedHelp() = "`~help` Get help"
    override fun helpCommandEmbedRank() = "`~rank` Show ranking from 1st to 10th"
    override fun helpCommandEmbedRating() = "`~rating`"

    override fun helpCommandEmbedLang(langList: String) =
        "`~lang` $langList Change the language setting used on this server. Ex) `~lang` `ENG`"
    override fun helpCommandEmbedStyle() =
        "`~skin` `A` `B` `C` Change the Gomoku-canvas style setting used on this server. Ex) `~skin` `A`"

    override fun helpCommandEmbedStartPVE() = "`~start` Start the game with A.I."
    override fun helpCommandEmbedStartPVP() =
        "`~start` `@mention` Start the game with the mentioned player. Ex) `~start` `@player`"
    override fun helpCommandEmbedResign() = "`~resign` Surrender the current game."

    // ## 1-2. RANK

    // ### 1-2-1. RANK (COMMAND)

    override fun rankCommand() = "rank"
    override fun rankCommandDescription() = "Show ranking from 1st to 10th"

    // ### 1-2-2. RANK:LIST (EMBED)

    override fun rankEmbedTitle() = "GomokuBot / Ranking"
    override fun rankEmbedDescription() = "Ranked 1st to 10th."
    override fun rankEmbedWin() = "Victory"
    override fun rankEmbedLose() = "Defeat"

    // ## 1-3. RATING

    // ### 1-3-1. RATING (COMMAND)

    override fun ratingCommand() = "rating"
    override fun ratingCommandDescription() = "Show rating specific User"
    override fun ratingCommandOptionUser() = "user"
    override fun ratingCommandOptionUserDescription() = "specific the user"

    // ### 1-3-2. RATING:RESPONSE (EMBED)

    override fun ratingEmbed() = TODO("Not yet implemented")
    override fun ratingEmbedDescription() = TODO("Not yet implemented")

    // # 2. CONFIG

    // ## 2-1. LANG

    // ### 2-1-1. LANG (COMMAND)

    override fun languageCommand() = "lang"
    override fun languageCommandDescription() = "Set language uses in this server"
    override fun languageCommandOptionCode() = "language"
    override fun languageCommandOptionCodeDescription() = "select language code"

    // ### 2-1-2. LANG:SUCCEED:CHANGED (MESSAGE)

    override fun languageUpdated() = "Language setting has been changed to English:flag_gb:!"

    // ## 2-2. STYLE

    // ### 2-2-1. STYLE (COMMAND)

    override fun styleCommand() = "style"
    override fun styleCommandDescription() = "Change Gomoku-board style setting used on this server."
    override fun styleCommandOptionCode() = "style"
    override fun styleCommandOptionCodeDescription() = "Select style code"

    // ### 2-2-2. STYLE:ENUM (EMBED)

    override fun styleEmbedTitle() = "GomokuBot / Style"
    override fun styleEmbedDescription() =
        "Default Gomoku board (Style A) may not display properly. " +
                "Choose one of the three styles available and set the style to use on this server."
    override fun styleEmbedSuggestion(styleName: String) = "Enter ``~skin`` ``$styleName`` to use this style."

    // ### 2-2-3. STYLE:ERROR:NOTFOUND (MESSAGE)

    override fun styleErrorNotfound() = "There is an error in the style specification."

    // ### 2-2-4. STYLE:SUCCEED:CHANGED (MESSAGE)

    override fun styleUpdated(styleName: String) = "Style setting has been change to ``$styleName`` !"

    // ## 2-3. POLICY

    // ### 2-3-1. POLICY (COMMAND)

    // # 3. SESSION

    override fun sessionNotFound(): String = ""

    // ## 3-1. START

    // ### 3-1-1. START (COMMAND)

    override fun startCommand() = "start"
    override fun startCommandDescription() = "Start a new game"
    override fun startCommandOptionOpponent() = "opponent"
    override fun startCommandOptionOpponentDescription() = "Specific the opponent"

    // ### 3-1-2. START:ERROR:ALREADY (MESSAGE)

    override fun startErrorSessionAlready(user: String) =
        "$user, Game creation failed. Please finish the game in progress. :thinking:"
    override fun startErrorRequestAlready(user: String) =
        ""

    // ## 3-2. SET

    // ### 3-2-1. SET (COMMAND)

    override fun setCommandDescription() = "돌을 놓습니다."
    override fun setCommandOptionColumn() = "x"
    override fun setCommandOptionColumnDescription() = "알파벳"
    override fun setCommandOptionRow() = "y"
    override fun setCommandOptionRowDescription() = "숫자"

    // ### 3-2-2. SET:ERROR:ARGUMENT (MESSAGE)

    override fun setErrorIllegalArgument(player: String) =
        "that's invalid command. Please write in the format of . `~s` `alphabet` `number` :thinking:"

    // ### 3-2-3. SET:ERROR:EXIST (MESSAGE)

    override fun setErrorExist(player: String, move: String) = "$player, there is already a stone. :thinking:"

    // ### 3-2-4. SET:ERROR:FORBIDDEN (MESSAGE)

    override fun setErrorForbidden(player: String, move: String, forbiddenKind: String) = ""

    // ## 3-3. RESIGN

    // ### 3-3-1. RESIGN (COMMAND)

    override fun resignCommand() = "resign"
    override fun resignCommandDescription() = "resign"

    // ### 3-3-2. RESIGN:ERROR:NOTFOUND (MESSAGE)

    // ## 3-4. REQUEST

    // ### 3-4-1. REQUEST:ABOUT (EMBED)

    override fun requestEmbedTitle() = "오목 한 판 괜찮겠습니까?"
    override fun requestEmbedDescription(owner: String, opponent: String) =
        "<@$owner> 님이 <@$opponent> 님에게 대결 요청을 보냈습니다. 아래 버튼을 눌러 대답해 주세요."
    override fun requestEmbedButtonAccept() = "Accept"
    override fun requestEmbedButtonReject() = "Reject"

    // ### 3-4-2. REQUEST:REJECTED (MESSAGE)

    override fun requestRejected(owner: String, opponent: String) =
        ""

    // # 4. GAME

    // ## 4-1. BEGIN

    // ### 4-1-1. BEGIN:PVP

    override fun beginPVP(owner: String, opponent: String, opener: String) =
        "The match between`$owner` vs `$opponent` has begun! Attack first is `$opener`."

    // ### 4-1-2. BEGIN:AI

    override fun beginPVE(player: String, opener: String) = ""

    // ## 4-2. PROCESS

    // ### 4-2-1. PROCES:NEXT (MESSAGE)

    override fun processNext(player: String, priorPlayer: String, latestMove: String) =
        "`$player`, please place the next Stone. `$priorPlayer` was placed on $latestMove"

    // ### 4-2-2. PROCESS:ERROR:ORDER (MESSAGE)

    override fun processErrorOrder(user: String, player: String) =
        "It's `$player`s turn now. Please wait for `$player`'s next stone. :thinking:"

    // ## 4-3. END

    // ### 4-3-1. END:PVP (MESSAGE)

    override fun endPVPWin(winner: String, looser: String, latestMove: String) =
        "`$winner` wins by `$looser` placing Stone on $latestMove!"
    override fun endPVPResign(winner: String, looser: String) =
        "`$winner` wins by `$looser` declaring surrender!"
    override fun endPVPTie(owner: String, opponent: String) = "There was no more space for the stones, so it was a draw."

    // ### 4-3-2. END:AI (MESSAGE)

    override fun endPVEWin(player: String, latestPos: String) =
       "You beat A.I. by placing Stone on $latestPos. Congratulations! :tada:"
    override fun endPVELose(player: String, latestPos: String) = "You have been defeated by A.I. placing on $latestPos."
    override fun endPVEResign(player: String) = "You have been defeated by declaring surrender."
    override fun endPVETie(player: String) = ""

    // # 5. BOARD

    override fun boardInProgress() = "In Progress"
    override fun boardFinished() = "Finished"

    override fun boardMoves() = "Moves"
    override fun boardLatestMove() = "Latest Move"

    override fun boardResult() = "Result"

    override fun boardWinDescription(winner: String) = "$winner win"
    override fun boardTieDescription() = "Tie"

    override fun boardCommandGuide() = ":mag: 버튼을 누르거나 ``/s`` ``알파벳`` ``숫자`` 명령어를 입력해 다음 수를 놓아 주세요."

    // # 6. UTILS

    override fun somethingWrongEmbedTitle() = "뭔가 잘못됐습니다!"

    // ## 6-1. PERMISSION-NOT-GRANTED (EMBED)

    override fun permissionNotGrantedEmbedDescription(channelName: String) = "Gokomubot은 ``$channelName``채널에 메시지를 보낼 권한이 없습니다! 역할 및 퍼미션 설정을 확인해 주세요."

    override fun permissionNotGrantedEmbedFooter() = "이 메시지는 1분 뒤 지워집니다."

    // ## 6-2. NOT-YET-IMPLEMENTED (EMBED)

    override fun notYetImplementedEmbedDescription() = "이 기능은 아직 완성되지 않았습니다."

    override fun notYetImplementedEmbedFooter() = "지원 채널에서 Gomokubot 업데이트 소식을 받아볼 수 있습니다."

}
