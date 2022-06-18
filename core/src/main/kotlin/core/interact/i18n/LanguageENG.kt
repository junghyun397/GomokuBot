package core.interact.i18n

open class LanguageENG : LanguageContainer {

    override fun languageCode() = "ENG"

    override fun languageName() = "\uD83C\uDDEC\uD83C\uDDE7 English"
    override fun languageSuggestion() = "Please use the ``/lang`` ``ENG`` command."

    // # 0. TOKENS

    override fun aiLevelAmoeba() = "Amoeba"
    override fun aiLevelApe() = "Ape"
    override fun aiLevelBeginner() = "Beginner"
    override fun aiLevelModerate() = "Moderate"
    override fun aiLevelExpert() = "Expert"
    override fun aiLevelGuru() = "Guru"

    // # 1. INFORM

    // ## 1-1. HELP

    // ### 1-1-1. HELP (COMMAND)

    override fun helpCommand() = "help"
    override fun helpCommandDescription() = "Get help."

    // ### 1-1-2. HELP:ABOUT (EMBED)

    override fun helpAboutEmbedTitle() = "GomokuBot / Help"
    override fun helpAboutEmbedDescription(platform: String) =
        "Now play **Gomoku** on **$platform**. **GomokuBot** can do it." +
                " ― GomokuBot is an open-source AI bot that provides Gomoku([Renju](https://www.renju.net/rules/)) feature in $platform. " +
                "The collected data are used for training reinforcement learning models."
    override fun helpAboutEmbedDeveloper() = "Developer"
    override fun helpAboutEmbedRepository() = "Git Repository"
    override fun helpAboutEmbedVersion() = "Version"
    override fun helpAboutEmbedSupport() = "Support Channel"
    override fun helpAboutEmbedInvite() = "Invite Link"

    // ### 1-1-3. HELP:COMMAND (EMBED)

    override fun helpCommandEmbedTitle() = "GomokuBot / Command"
    override fun helpCommandEmbedHelp() = "Get help."
    override fun helpCommandEmbedRank() = "Get a ranking from 1st to 10th."
    override fun helpCommandEmbedRating() = "Get ``GomokuBot ELO`` rating."

    override fun helpCommandEmbedLang(langList: String) =
        "Change the language setting used by this server. Ex) ``/lang`` ``ENG``"
    override fun helpCommandEmbedStyle() =
        "Change the Gomoku Board style used by this server. Ex) ``/style`` ``A``"

    override fun helpCommandEmbedStartPVE() = "Start a new game with AI."
    override fun helpCommandEmbedStartPVP() =
        "Send a game request to the mentioned user. Ex) ``/start`` ``@user``"
    override fun helpCommandEmbedResign() = "Resign from a game in progress."

    // ## 1-2. RANK

    // ### 1-2-1. RANK (COMMAND)

    override fun rankCommand() = "rank"
    override fun rankCommandDescription() = "Get a ranking from 1st to 10th."

    // ### 1-2-2. RANK:LIST (EMBED)

    override fun rankEmbedTitle() = "GomokuBot / Ranking"
    override fun rankEmbedDescription() = "Ranking from 1st to 10th."
    override fun rankEmbedWin() = "Wins"
    override fun rankEmbedLose() = "Losses"

    // ## 1-3. RATING

    // ### 1-3-1. RATING (COMMAND)

    override fun ratingCommand() = "rating"
    override fun ratingCommandDescription() = "Get rating."
    override fun ratingCommandOptionUser() = "user"
    override fun ratingCommandOptionUserDescription() = "Specific a user to check the rating."

    // ### 1-3-2. RATING:RESPONSE (EMBED)

    override fun ratingEmbed() = TODO("Not yet implemented")
    override fun ratingEmbedDescription() = TODO("Not yet implemented")

    // # 2. CONFIG

    // ## 2-1. LANG

    // ### 2-1-1. LANG (COMMAND)

    override fun languageCommand() = "lang"
    override fun languageCommandDescription() = "Change the language setting used by this server."
    override fun languageCommandOptionCode() = "language"
    override fun languageCommandOptionCodeDescription() = "Select a language code."

    // ### 2-1-2. LANG:SUCCEED:CHANGED (MESSAGE)

    override fun languageUpdated() = "Language setting has been changed to English:flag_gb:!"

    // ## 2-2. STYLE

    // ### 2-2-1. STYLE (COMMAND)

    override fun styleCommand() = "style"
    override fun styleCommandDescription() = "Change the Gomoku Board style used by this server."
    override fun styleCommandOptionCode() = "style"
    override fun styleCommandOptionCodeDescription() = "Select a style code."

    // ### 2-2-2. STYLE:LIST (EMBED)

    override fun styleEmbedTitle() = "GomokuBot / Style"
    override fun styleEmbedDescription() =
        "Default Gomoku Board style(``Style A``) applied to this server may not display correctly. " +
                "Choose one of the four styles you like."
    override fun styleEmbedSuggestion(styleName: String) = "Enter ``/style`` $styleName to use this style."

    // ### 2-2-3. STYLE:ERROR:NOTFOUND (MESSAGE)

    override fun styleErrorNotfound(user: String) =
        "$user, There is an error in the specification style code. Please enter in ``/style`` ``style code`` format."

    // ### 2-2-4. STYLE:SUCCEED:CHANGED (MESSAGE)

    override fun styleUpdated(styleName: String) =
        "Style setting has been changed to style ``$styleName``!"

    // ## 2-3. POLICY

    override fun configApplied(choice: String) = "$choice Setting has been applied to this server."

    // ### 2-3-1. STYLE

    override fun styleSelectImage() = "Image"
    override fun styleSelectImageDescription() =
        "Render as an Image. Depending on the status of the platform server, there may be some delays."

    override fun styleSelectText() = "Text"
    override fun styleSelectTextDescription() = "Render as a Monospaced Text. The fastest."

    override fun styleSelectSolidText() = "Solid Text"
    override fun styleSelectSolidTextDescription() = "Same as Text - but with dots instead of blanks."

    override fun styleSelectUnicodeText() = "Unicode"
    override fun styleSelectUnicodeTextDescription() =
        "Render as Unicode characters. Depending on the font settings, it may look broken."

    // ### 2-3-2. FOCUS

    override fun focusEmbedTitle() = "GomokuBot / Focus"
    override fun focusEmbedDescription() =
        "GomokuBot uses a small-sized \"Button Board\" for intuitive input. Please set how the GomokuBot should zoom in on the board."

    override fun focusSelectIntelligence() = "Intelligence"
    override fun focusSelectIntelligenceDescription() =
        "The GomokuBot inference engine will focus on the most optimal places."

    override fun focusSelectFallowing() = "Fallow"
    override fun focusSelectFallowingDescription() =
        "Always focus on the last move."

    // ### 2-3-3. SWEEP

    override fun sweepEmbedTitle() = "GomokuBot / Sweep"
    override fun sweepEmbedDescription() =
        "GomokuBot sends very, very many messages. Please set what to do with the message sent by GomokuBot."

    override fun sweepSelectRelay() = "Relay"
    override fun sweepSelectRelayDescription() =
        "When a player makes a new move, clear all previously sent messages."

    override fun sweepSelectLeave() = "Leave"
    override fun sweepSelectLeaveDescription() =
        "Do not delete any messages. Except for the Navigators."

    // ### 2-3-4. ARCHIVE

    override fun archiveEmbedTitle() = "GomokuBot / Archive"
    override fun archiveEmbedDescription() =
        "GomokuBot archives players' awesome game results to the official channel of GomokuBot. " +
                "Of course, GomokuBot places predominant on player privacy. Please set how you want to archive the results of the game."

    override fun archiveSelectByAnonymous() = "Anonymous"
    override fun archiveSelectByAnonymousDescription() =
        "Share player's game results anonymously."

    override fun archiveSelectWithProfile() = "By Profile"
    override fun archiveSelectWithProfileDescription() =
        "Share player's game results with their profile picture and name."

    override fun archiveSelectPrivacy() = "Keep Privacy"
    override fun archiveSelectPrivacyDescription() =
        "Don't share player's game results with anyone."

    // # 3. SESSION

    override fun sessionNotFound(user: String): String =
        "$user, There is no game in progress. Start a new game with the ``/start`` command."

    // ## 3-1. START

    // ### 3-1-1. START (COMMAND)

    override fun startCommand() = "start"
    override fun startCommandDescription() = "Start a new game."
    override fun startCommandOptionOpponent() = "opponent"
    override fun startCommandOptionOpponentDescription() = "Specific the user to start game with."

    // ### 3-1-2. START:ERROR:ALREADY (MESSAGE)

    override fun startErrorSessionAlready(user: String) =
        "$user, There is already a game in progress. Please finish the game in progress first."
    override fun startErrorOpponentSessionAlready(owner: String, opponent: String) =
        "$owner, $opponent is already playing another game. Please wait until $opponent's game is over."
    override fun startErrorRequestAlreadySent(owner: String, opponent: String) =
        "$owner, a game request sent to $opponent is still pending. Please wait for $opponent's response."
    override fun startErrorRequestAlready(user: String, opponent: String) =
        "$user, You have not yet responded to the game request sent by $opponent. Please respond to $opponent's game request first."
    override fun startErrorOpponentRequestAlready(owner: String, opponent: String) =
        "$owner, There is one other game request that $opponent has not yet responded to. Please wait until $opponent responds to another game request."

    // ## 3-2. SET

    // ### 3-2-1. SET (COMMAND)

    override fun setCommandDescription() = "Make a move."
    override fun setCommandOptionColumn() = "column"
    override fun setCommandOptionColumnDescription() = "alphabet"
    override fun setCommandOptionRow() = "row"
    override fun setCommandOptionRowDescription() = "number"

    // ### 3-2-2. SET:ERROR:ARGUMENT (MESSAGE)

    override fun setErrorIllegalArgument(player: String) =
        "$player, There is an error in the command format. Please enter in ``/s`` ``alphabet`` ``number`` format."

    // ### 3-2-3. SET:ERROR:EXIST (MESSAGE)

    override fun setErrorExist(player: String, move: String) =
        "$player, There is already a stone in $move. Please move to another place."

    // ### 3-2-4. SET:ERROR:FORBIDDEN (MESSAGE)

    override fun setErrorForbidden(player: String, move: String, forbiddenKind: String) =
        "$player, ``$move`` is ``$forbiddenKind`` forbidden move. Please move to another place."

    // ## 3-3. RESIGN

    // ### 3-3-1. RESIGN (COMMAND)

    override fun resignCommand() = "resign"
    override fun resignCommandDescription() = "Resigns from a game in progress."

    // ### 3-3-2. RESIGN:ERROR:NOTFOUND (MESSAGE)

    // ## 3-4. REQUEST

    // ### 3-4-1. REQUEST:ABOUT (EMBED)

    override fun requestEmbedTitle() = "How about a round of Gomoku?"
    override fun requestEmbedDescription(owner: String, opponent: String) =
        "$owner sent a game request to $opponent. Please respond by pressing the button."
    override fun requestEmbedButtonAccept() = "Accept"
    override fun requestEmbedButtonReject() = "Reject"

    // ### 3-4-2. REQUEST:REJECTED (MESSAGE)

    override fun requestRejected(owner: String, opponent: String) =
        "$opponent rejected $owner's game request."

    override fun requestExpired(owner: String, opponent: String) =
        "Game request that $owner sent to $opponent has expired. If anyone still wants to game with $opponent, please send a new request."

    override fun requestExpiredNewRequest() =
        "re-Request"

    // # 4. GAME

    // ## 4-1. BEGIN

    // ### 4-1-1. BEGIN:PVP

    override fun beginPVP(blackPlayer: String, whitePlayer: String) =
        "The game of $blackPlayer vs $whitePlayer has started! $blackPlayer is Black. Please make the first move."

    // ### 4-1-2. BEGIN:AI

    override fun beginPVEAiBlack(player: String) =
        "The game of $player vs AI has started! $player is White. AI placed ``h8``. Please make the next move."

    override fun beginPVEAiWhite(player: String) =
        "The game of $player vs AI has started! $player is Black. Please make the first move."

    // ## 4-2. PROCESS

    // ### 4-2-1. PROCES:NEXT (MESSAGE)

    override fun processNextPVE(owner: String, latestMove: String) =
        "$owner, Please make the next move. AI placed $latestMove."

    override fun processNextPVP(player: String, priorPlayer: String, latestMove: String) =
        "$player, Please make the next move. $priorPlayer placed $latestMove."

    // ### 4-2-2. PROCESS:ERROR:ORDER (MESSAGE)

    override fun processErrorOrder(user: String, player: String) =
        "$user, Now it's $player's turn. Please wait until $player makes the next move."

    // ## 4-3. END

    // ### 4-3-1. END:PVP (MESSAGE)

    override fun endPVPWin(winner: String, looser: String, latestMove: String) =
        "$winner wins by $looser placed in $latestMove!"
    override fun endPVPResign(winner: String, looser: String) =
        "$winner wins by $looser resignation!"
    override fun endPVPTie(owner: String, opponent: String) =
        "$owner vs $opponent ended in a draw because there were no more points to make a move."
    override fun endPVPTimeOut(winner: String, looser: String) =
        "$winner wins by $looser because $looser didn't make the next move for a long time."

    // ### 4-3-2. END:AI (MESSAGE)

    override fun endPVEWin(player: String, latestPos: String) =
        "$player, You won to AI by placed in $latestPos."
    override fun endPVELose(player: String, latestPos: String) =
        "$player, You lose to AI by AI placed in $latestPos."
    override fun endPVEResign(player: String) =
        "$player, You lose to AI by resignation."
    override fun endPVETie(player: String) =
        "$player vs AI ended in a draw because there were no more points to make a move."
    override fun endPVETimeOut(player: String) =
        "$player, You lost to Ai because you didn't make the next move for a long time."

    // # 5. BOARD

    override fun boardInProgress() = "In Progress"
    override fun boardFinished() = "Finished"

    override fun boardMoves() = "Moves"
    override fun boardLatestMove() = "Last Move"

    override fun boardResult() = "Result"

    override fun boardWinDescription(winner: String) = "$winner win"
    override fun boardTieDescription() = "Tie"

    override fun boardCommandGuide() =
        ":mag: Press the button or use ``/s`` ``column`` ``row`` command to make the next move."

    // # 6. UTILS

    override fun somethingWrongEmbedTitle() = "Something Wrong"

    // ## 6-1. PERMISSION-NOT-GRANTED (EMBED)

    override fun permissionNotGrantedEmbedDescription(channelName: String) =
        "GomokuBot dose not has permission to send messages to $channelName! Please check the role and permission settings."

    override fun permissionNotGrantedEmbedFooter() = "this message will be deleted after a minute."

    // ## 6-2. NOT-YET-IMPLEMENTED (EMBED)

    override fun notYetImplementedEmbedDescription() = "This feature is not yet implemented."

    override fun notYetImplementedEmbedFooter(officialChannel: String) = "Get updates on GomokuBot in the support channel($officialChannel)."

}
