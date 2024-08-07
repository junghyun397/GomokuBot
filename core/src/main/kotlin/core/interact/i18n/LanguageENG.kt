package core.interact.i18n

import core.assets.UNICODE_RIGHT

open class LanguageENG : LanguageContainer {

    override fun languageCode() = "ENG"

    override fun languageName() = "\uD83C\uDDEC\uD83C\uDDE7 English"
    override fun languageSuggestion() = "Please use the ``/lang`` ``ENG`` command."

    override fun aiLevelAmoeba() = "Amoeba"
    override fun aiLevelApe() = "Ape"
    override fun aiLevelBeginner() = "Beginner"
    override fun aiLevelIntermediate() = "Intermediate"
    override fun aiLevelAdvanced() = "Advanced"
    override fun aiLevelExpert() = "Expert"
    override fun aiLevelGuru() = "Guru"

    override fun swapSelectYes() = "Yes"
    override fun swapSelectNo() = "No"

    override fun branchSelectSwap() = "Swap"
    override fun branchSelectOffer() = "Offer"

    override fun ruleSelectRenju() = "Renju (Default)"
    override fun ruleSelectTaraguchi10() = "Taraguchi-10"
    override fun ruleSelectSoosyrv8() = "Soosyrv-8"

    override fun helpCommand() = "help"
    override fun helpCommandDescription() = "Get help."
    override fun helpCommandOptionShortcut() = "shortcut"
    override fun helpCommandOptionShortcutDescription() = "Quickly navigate to the specified help page."
    override fun helpCommandOptionAnnouncements() = "announcements"

    override fun settingsCommand() = "settings"
    override fun settingsCommandDescription() = "Get settings panel."

    override fun helpAboutEmbedTitle() = "GomokuBot / Help"
    override fun helpAboutEmbedDescription(platform: String) =
        "Now play **Gomoku** on **$platform**. **GomokuBot** can do it." +
                " ― GomokuBot is an AI bot that provides Gomoku([Renju](https://www.renju.net/rules/)) feature in $platform. " +
                "The collected data are used for training reinforcement learning models."
    override fun helpAboutEmbedDeveloper() = "Developer"
    override fun helpAboutEmbedRepository() = "Git Repository"
    override fun helpAboutEmbedVersion() = "Version"
    override fun helpAboutEmbedSupport() = "Support Channel"
    override fun helpAboutEmbedInvite() = "Invite Link"

    // chunk

    override fun commandUsageEmbedTitle() = "GomokuBot / Commands"
    override fun commandUsageHelp() = "Get help."
    override fun commandUsageSettings() = "Get settings panel."
    override fun commandUsageRankGlobal() = "Get the overall ranking of GomokuBot from 1st to 10th."
    override fun commandUsageRankServer() = "Get the internal ranking of this server."
    override fun commandUsageRankUser() = "Get a ranking of mentioned user's opponents."
    override fun commandUsageReplay() = "Get a list of recently played game replays."
    override fun commandUsageRating() = "Get ``GomokuBot ELO`` rating."

    override fun commandUsageLang(langList: String) =
        "Change the language setting used by this server. Ex) ``/lang`` ``ENG``"
    override fun commandUsageStyle() =
        "Change the Gomoku Board style used by this server. Ex) ``/style`` ``A``"

    override fun commandUsageStartPVE() = "Start a new game with AI."
    override fun commandUsageStartPVP() =
        "Send a game request to the mentioned user. Ex) ``/start`` ``@user``"
    override fun commandUsageResign() = "Resign from a game in progress."

    override fun commandUsageBoard() = "Opens the game currently in progress as a new message."

    override fun replayCommand() = "replay"
    override fun replayCommandDescription() = "Replay recently played games."

    // chunk

    override fun rankCommand() = "rank"
    override fun rankCommandDescription() = "Get a ranking from 1st to 10th."
    override fun rankCommandSubGlobal() = "global"
    override fun rankCommandSubGlobalDescription() = "Get the Gomokubot overall ranking."
    override fun rankCommandSubServer() = "server"
    override fun rankCommandSubServerDescription() = "Get internal server ranking."
    override fun rankCommandSubUser() = "user"
    override fun rankCommandSubUserDescription() = "Get user-opponents ranking."
    override fun rankCommandOptionPlayer() = "player"
    override fun rankCommandOptionPlayerDescription() = "Specific a player to check the opponent ranking."

    override fun rankErrorNotFound() = "User record not found. Please specify a user who has played GomokuBot PvP."

    override fun rankEmbedTitle() = "GomokuBot / Ranking"
    override fun rankEmbedDescription() = "Ranking from 1st to 10th."
    override fun rankEmbedWin() = "Wins"
    override fun rankEmbedLose() = "Losses"
    override fun rankEmbedDraw() = "Draws"

    override fun ratingCommand() = "rating"
    override fun ratingCommandDescription() = "Get rating."
    override fun ratingCommandOptionUser() = "user"
    override fun ratingCommandOptionUserDescription() = "Specific a user to check the rating."

    override fun ratingEmbed() = TODO("Not yet implemented")
    override fun ratingEmbedDescription() = TODO("Not yet implemented")

    override fun languageCommand() = "lang"
    override fun languageCommandDescription() = "Change the language setting used by this server."
    override fun languageCommandOptionCode() = "language"
    override fun languageCommandOptionCodeDescription() = "Select a language code."

    override fun languageUpdated() = "Language setting has been changed to English:flag_gb:!"

    // chunk

    override fun styleCommand() = "style"
    override fun styleCommandDescription() = "Change the Gomoku Board style used by this server."
    override fun styleCommandOptionCode() = "style"
    override fun styleCommandOptionCodeDescription() = "Select a style code."

    override fun styleEmbedTitle() = "GomokuBot / Style"
    override fun styleEmbedDescription() =
        "Default Gomoku Board style(``Style A``) applied to this server may not display correctly. " +
                "Choose one of the four styles you like."
    override fun styleEmbedSuggestion(styleName: String) = "Enter ``/style`` $styleName to use this style."

    override fun styleErrorNotfound() =
        "There is an error in the specification style code. Please enter in ``/style`` ``style code`` format."

    override fun styleUpdated(styleName: String) =
        "Style setting has been changed to style ``$styleName``!"

    override fun settingApplied(kind: String, choice: String) = "$kind setting has been changed to $choice."

    override fun style() = "Style"

    override fun styleSelectImage() = "Image"
    override fun styleSelectImageDescription() =
        "Render as an Image. Depending on the status of the platform server, there may be some delays."

    override fun styleSelectText() = "Text"
    override fun styleSelectTextDescription() = "Render as a Monospaced Text. The fastest."

    override fun styleSelectDottedText() = "Solid Text"
    override fun styleSelectDottedTextDescription() = "Same as Text - but with dots instead of blanks."

    override fun styleSelectUnicodeText() = "Unicode"
    override fun styleSelectUnicodeTextDescription() =
        "Render as Unicode characters. Depending on the font settings, it may look broken."

    // chunk

    override fun focus() = "Focus"

    override fun focusEmbedTitle() = "GomokuBot / Focus"
    override fun focusEmbedDescription() =
        "GomokuBot uses a small-sized \"Button Board\" for intuitive input. Please set how the GomokuBot should zoom in on the board."

    override fun focusSelectIntelligence() = "Intelligence"
    override fun focusSelectIntelligenceDescription() =
        "The GomokuBot inference engine will focus on the most optimal places."

    override fun focusSelectFallowing() = "Fallow"
    override fun focusSelectFallowingDescription() =
        "Always focus on the last move."

    override fun hint() = "Hint"

    override fun hintEmbedTitle()= "GomokuBot / Hint"
    override fun hintEmbedDescription() =
        "Gomoku has important moves decide whether lose or not. Please set how GomokuBot emphasizes important moves."

    override fun hintSelectFive() = "Five"
    override fun hintSelectFiveDescription() = "Highlight the move to create a five-in-a-row."

    override fun hintSelectOff() = "Off"
    override fun hintSelectOffDescription() = "Do not highlight any moves."

    // chunk

    override fun mark() = "Mark"

    override fun markEmbedTitle() = "Gomokubot / Mark"
    override fun markEmbedDescription() =
        "It's not an easy task to memorize the last move among the many stones. Please set how Gomokubot display your last move. "

    override fun markSelectLast() = "Last Move"
    override fun markSelectLastDescription() =
        "Draw a small dot where the opponent last moved."

    override fun markSelectRecent() = "Recent Moves"
    override fun markSelectRecentDescription() =
        "Draw a small dot where the opponent last moved and thin cross at your last moved."

    override fun markSelectSequence() = "Sequences"
    override fun markSelectSequenceDescription() =
        "Mark all the stones in the order in which they where moved."

    override fun swap() = "Swap"

    override fun swapEmbedTitle() = "GomokuBot / Swap"
    override fun swapEmbedDescription() =
        "GomokuBot sends very, very many messages. Please set what to do with the message sent by GomokuBot."

    override fun swapSelectRelay() = "Relay"
    override fun swapSelectRelayDescription() =
        "When a player makes a new move, clear all previously sent messages."

    override fun swapSelectArchive() = "Archive"
    override fun swapSelectArchiveDescription() =
        "Do not delete any messages. Except for the Navigators."

    override fun swapSelectEdit() = "Edit"
    override fun swapSelectEditDescription() =
        "Send no more messages, edit the first message sent."

    // chunk

    override fun archive() = "Archive"

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

    // chunk

    override fun sessionNotFound(): String =
        "There is no game in progress. Start a new game with the ``/start`` command."

    override fun startCommand() = "start"
    override fun startCommandDescription() = "Start a new game."
    override fun startCommandOptionOpponent() = "opponent"
    override fun startCommandOptionOpponentDescription() = "Specific the user to start game with."
    override fun startCommandOptionRule() = "rule"
    override fun startCommandOptionRuleDescription() = "Specific the rules for the new game."

    override fun startErrorSessionAlready() =
        "There is already a game in progress. Please finish the game in progress first."
    override fun startErrorOpponentSessionAlready(opponent: String) =
        "$opponent is already playing another game. Please wait until $opponent's game is over."
    override fun startErrorRequestAlreadySent(opponent: String) =
        "A game request sent to $opponent is still pending. Please wait for $opponent's response."
    override fun startErrorRequestAlready(opponent: String) =
        "You have not yet responded to the game request sent by $opponent. Please respond to $opponent's game request first."
    override fun startErrorOpponentRequestAlready(opponent: String) =
        "There is one other game request that $opponent has not yet responded to. Please wait until $opponent responds to another game request."

    override fun setCommandDescription() = "Make a move."
    override fun setCommandOptionColumn() = "column"
    override fun setCommandOptionColumnDescription() = "alphabet"
    override fun setCommandOptionRow() = "row"
    override fun setCommandOptionRowDescription() = "number"

    override fun setErrorIllegalArgument() =
        "There is an error in the command format. Please enter in ``/s`` ``alphabet`` ``number`` format."

    override fun setErrorExist(move: String) =
        "There is already a stone in $move. Please move to another place."

    override fun setErrorForbidden(move: String, forbiddenKind: String) =
        "$move is $forbiddenKind forbidden move. Please move to another place."

    // chunk

    override fun resignCommand() = "resign"
    override fun resignCommandDescription() = "Resigns from a game in progress."

    override fun boardCommand() = "board"
    override fun boardCommandDescription() = "Opens the game currently in progress as a new message."

    override fun requestEmbedTitle() = "How about a game of Gomoku?"
    override fun requestEmbedDescription(owner: String, opponent: String) =
        "$owner sent a game request to $opponent. Please respond by pressing the button."
    override fun requestEmbedButtonAccept() = "Accept"
    override fun requestEmbedButtonReject() = "Reject"

    override fun requestRejected(owner: String, opponent: String) =
        "$opponent rejected $owner's game request."

    override fun requestExpired(owner: String, opponent: String) =
        "Game request that $owner sent to $opponent has expired. If anyone still wants to game with $opponent, please send a new request."

    override fun requestExpiredNewRequest() =
        "re-Request"

    // chunk

    override fun beginPVP(blackPlayer: String, whitePlayer: String) =
        "The game of $blackPlayer vs $whitePlayer has started! $blackPlayer is Black. Please make the first move."

    override fun beginOpening(blackPlayer: String, whitePlayer: String) =
        "The opening renju game of $blackPlayer vs $whitePlayer has started! $blackPlayer is black. $whitePlayer needs to decide whether to swap to black or play as is."

    override fun beginPVEAiBlack(player: String) =
        "The game of $player vs AI has started! $player is White. AI made a move at ``h8``. Please make the next move."

    override fun beginPVEAiWhite(player: String) =
        "The game of $player vs AI has started! $player is Black. Please make the first move."

    override fun processNextPVE(lastMove: String) =
        "Please make the next move. AI made a move at $lastMove."

    override fun processNextPVP(priorPlayer: String, lastMove: String) =
        "Please make the next move. $priorPlayer have placed at $lastMove."

    override fun processNextOpening(lastMove: String) =
        "Placed the stone at $lastMove. Please follow the next opening procedure."

    override fun processErrorOrder(player: String) =
        "Now it's $player's turn. Please wait until $player makes the next move."

    override fun endPVPWin(winner: String, loser: String, lastMove: String) =
        "$winner wins by $loser placed at $lastMove."
    override fun endPVPResign(winner: String, loser: String) =
        "$winner wins by $loser resignation."
    override fun endPVPTie(owner: String, opponent: String) =
        "$owner vs $opponent ended in a draw because there were no more points to make a move."
    override fun endPVPTimeOut(winner: String, loser: String) =
        "$winner wins by $loser because $loser didn't make the next move for a long time."

    override fun endPVEWin(player: String, lastPos: String) =
        "$player, You won to AI by placed at $lastPos."
    override fun endPVELose(player: String, lastPos: String) =
        "$player, You lose to AI by AI placed at $lastPos."
    override fun endPVEResign(player: String) =
        "$player, You lose to AI by resignation."
    override fun endPVETie(player: String) =
        "$player vs AI ended in a draw because there were no more points to make a move."
    override fun endPVETimeOut(player: String) =
        "$player, You lost to AI because you didn't make the next move for a long time."

    // chunk

    override fun boardInProgress() = "In Progress"
    override fun boardInOpening() = "In Opening"
    override fun boardFinished() = "Finished"

    override fun boardMoves() = "Moves"
    override fun boardLastMove() = "Last Move"

    override fun boardResult() = "Result"

    override fun boardWinDescription(winner: String) = "$winner win"
    override fun boardTieDescription() = "Tie"

    override fun boardCommandGuide() =
        ":mag: Press the button or use ``/s`` ``column`` ``row`` command to make the next move."
    override fun boardSwapGuide() =
        ":arrows_counterclockwise: Press the button to select whether to switch between black and white."
    override fun boardStatefulSwapGuide(offerCount: Int) =
        ":arrows_counterclockwise: Press the button to select whether to switch between black and white. The number of 5th move candidates that the black player should offer is ``$offerCount``."
    override fun boardBranchGuide() =
        ":paperclips: Press the button to choose whether you want to take the opportunity to swap black and white, or offer 10 possible 5th move candidates to opponent."
    override fun boardDeclareGuide() =
        ":paperclips: Use the Select menu to choose how many 5th move candidates to pick."
    override fun boardSelectGuide() =
        ":dart: Press the button or use ``/s`` ``column`` ``row`` command to select 5th move."
    override fun boardOfferGuide(remainingMoves: Int) =
        ":question: Press the button or use ``/s`` ``column`` ``row`` command to pick ``$remainingMoves`` candidates for the 5th move."

    override fun replayEmbedWin() = "Win"
    override fun replayEmbedLose() = "Lose"
    override fun replayEmbedDraw() = "Draw"
    override fun replayEmbedMatchInfo(totalMoves: Int) = "total $totalMoves moves."
    override fun replayEmbedUnableToReplayDescription() = "This is an empty match. The replay is unavailable. Please select another match."

    override fun announceWrittenOn(date: String) = "Written on $date."

    override fun somethingWrongEmbedTitle() = "Something Wrong"

    override fun permissionNotGrantedEmbedDescription(channelName: String) =
        "GomokuBot dose not has permission to send messages to $channelName! Please check the role and permission settings."

    override fun permissionNotGrantedEmbedFooter() = "this message will be deleted after a minute."

    override fun notYetImplementedEmbedDescription() = "This feature is not yet implemented."

    override fun notYetImplementedEmbedFooter() =
        "Get updates on GomokuBot in the support channel(https://discord.gg/vq8pkfF)."

    override fun exploreAboutRenju() = "Don't know what Renju is? Press $UNICODE_RIGHT to learn about Renju."

    override fun aboutRenjuDocument() = """
## What is Renju? {#about-renju}

Q. What do you mean, GomokuBot and Renju?

A. Gomoku is elementary. But Gomoku is not a fair game. Therefore, GomokuBot uses Renju, which has some additional rules, not plain Gomoku.

But don't worry. Renju is really similar to Gomoku. In the game between beginners, it's the same so that, even if they don't know what Renju is, it doesn't affect them at all.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/intro.png)

*Black wins!*

Renju is a variant game that adds **Forbidden moves** that only apply to black in Gomoku. Before diving into Renju and Forbidden moves, we need to understand why Renju only applies Forbidden moves to black, and how unfair a plain Gomoku game is.

### Gomoku is a Solved Game.

If you've played Gomoku for a while, you've probably figured out that black is very advantageous to start playing first on Gomoku, where there are no other constraints. So, how good is black? What would be the result if both black and white had their best moves?

A plain Gomoku with no additional rules was proved in 1980 by Stefan Reisch.[*](https://doi.org/10.1007/bf00288536) Even if both black and white have their best moves, black can always find a winning strategy.

In other words, in a plain Gomoku, black **always** wins, even if both black and white have their best moves. The higher the level of both players, the closer they get to tossing a coin. In order to get out of the coin toss, a special rule is indispensable to solve the overwhelming advantage of black first.

### Renju has "Forbidden Moves"

Renju chose the **Forbidden moves** rule to address the overwhelming advantage of black First. There are three types of forbids: 3-3 forbid, 4-4 forbid, and overline forbid.

If it looks complicated, don't worry. Forbidden moves are fairly rare in beginners' games, and even if they don't know what a Forbidden move is, it's unlikely to change their win or loss.

To understand forbidden moves correctly, you must first understand how Gomoku defines three and four, and in which situations the forbidden moves appears. Here we start with the definition of four.

## What is "Four"? {#four}

**Four** is **a row of four stones containing one space**, which means a shape that can be won by adding one more stone.

You can win by making one more move, so if you don't have four, the opponent makes four, you have to block immediately.

The four stones arranged in a straight line are four. Four stone blocks on one side are also four. The four stones arranged one space apart are also four. If you can make a five-in-a-row with a single move, it's all four.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/four.png)

*When making one more move…*

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/four-expanded.png)
*...Black can win!*

## What is "4-4 Forbid"? {#4-4forbid}

**4-4 forbid** means **a point where you can make two or more fours with a single move**. Even if you can create three fours in a single move, it is 4-4 forbid.

Remember: forbidden moves are rules designed to solve Black's overwhelming advantage. Therefore, all forbidden moves apply only to black. White is free to make 4-4 fork to win!

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/double-four-forbid.png)

*GomokuBot marks forbidden moves with a red dot. Black cannot place stones on the red dots, but white can.*

Although less common, more than one 4-4 forbid can appear on the same line. If you can make more than one four with a single move, even on the same line, it's 4-4 forbid.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/double-four-forbid-in-a-line.png)

## What is "Three"? {#three}

Three is a little special. That's because it's a bit far from Gomoku's win conditions. Gomoku defines three as a shape that can make a "straight four" in a single move. What exactly is straight four defined by Gomoku?

### Straight Four – Some Fours Are More Strong Than Others.

Here are the five fours we looked at two pages ago. In fact, one of the four has one difference from the other four. This is because the fours in row 8 are **straight fours**.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/straight-four.png)

*If you don't block immediately, you lose, so you have to block four right away. Let's defend.*

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/straight-four-had-blocked.png)

The other four could have been blocked, but the four in row 8 could not be blocked with a single move. You have to move twice to block, but in Gomoku you can't block because you can only move once per turn.

In this way, the strong four that cannot be blocked with a single move, and the four stones arranged in a row with both sides open are called straight four.

### If You Can Make a Straight Four With a Single Move, It's Three.

Gomoku defines **three** as **the shape that can make a straight four in a single move**. Three is a weaker shape than four, but it's a strong shape that should be blocked right away if your opponent makes three when you don't have three or four.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/three.png)

*When making one more move…*

*…Straight four are here!*

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/three-expanded.png)

## What is "3-3 Forbid"? {#3-3forbid}

Now that we know what three is defined by Gomoku, we can clearly define 3-3 forbid. **3-3 forbid** means **a position where you can make two or more threes that can make straight four by single move**. Even if you can make three threes with a single move, it's even 3-3 forbid.

*Please remember again. All forbidden moves apply only to black. White is free to make 3-3 to win.*

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/dobule-three-forbid.png)

So far, we have looked at elementary shapes, but in real games, sometimes complex shapes appear. Here are some simple examples. The shapes below do not contain any 3-3 forbids.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/dobule-three-forbid-pseudo.png)

This is because all these shapes are not three overlapping shapes, but actually only three shapes. Even if it looks like three, it's not three if you can't make straight four out of it. If you can't make more than one three in a single move, it's not 3-3 forbid.

## What is "Overline Forbid"? {#overlineforbid}

We're almost there. An **overline forbid** means **a point where you can make 6 or more stones arranged in a row in a single move**.

Even if you can make a 7-in-a-row in a single move, it's overline forbid. Neither 8-in-a-row nor 9-in-a-row. However, 10-in-a-row is an exception. If someone makes a 10-in-a-row, you must immediately turn off all electronics and leave the area.

*Please remember. Overline forbid is also a rule that only applies to black, and white is free to create more than five-in-a-row to win.*

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/overline-forbid.png)

## Five-in-a-Row Can Ignore All Forbidden Moves.

Here's the good news (only black, unfortunately). Even if the forbidden move is created on a point that can be won with a five-in-a-row, if you win with a five-in-a-row, you can move ignoring any forbidden moves.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/five-in-a-row-and-forbid.png)

## Great!

Welcome to the much fairer Gomoku! If you've followed this guide far, you know all the basic Renju rules. Now, even if forbidden moves appear, you will be able to unlock that or use other strategies to continue the game without panicking.

Now start a game with your friends with the ``/start @mention`` command. Even if you don't have friends, the GomokuBot AI will always be with you. Don't forget to customize GomokuBot with the ``/setting`` command as well.

In the next chapter, we'll learn how to figure out forbidden moves in very complex situations and how to attack and defend using forbidden moves. These are strategies for Renju, all possible only in Renju. It can be a little difficult for beginners.

## Like a Forbidden Move, But May Not Be a Forbidden Move. {#pseudo-forbid}

Consider the following situation. Will black be able to move with ``h9``? At first glance, it seems that ``h9`` is a 3-3 forbid made by two stones arranged vertically in column h and two stones arranged horizontally in row 9, and it seems that black should not be able to move with ``i9``.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/pseudo-forbid-simple.png)

While these situations aren't common(in fact, they're really, very rare in real games), but you need to be aware of them in order to fully understand what forbidden moves are.

### Imagine What Happens Next.

In complex situations, a good way to figure out forbidden moves is to move one step at a time. First, let's make move to ``h9``, which is the point that we want to figure out.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/pseudo-forbid-simple-s1.png)

In this situation, column h is blocked by 4-4 forbid and cannot create straight four. Two stones in column h were not three that could make a straight four in a single move. Let's remember the definition of forbidden moves again. If three or four is not valid, then the forbidden move is also invalid. So ``h9`` that can only make one three is not 3-3 forbidden.

## It Doesn't Seem Like a Forbidden Move, But It Could Be a Forbidden Move. {#complex-pseudo-forbid}

The ``g10`` looks like 3-3 forbid. But since ``i8`` is also forbid, ``g10`` doesn't seem to be forbid 3-3 forbid. How can we figure out how black can move in ``g10`` in this complex situation?

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/pseudo-forbid-complex.png)

### Imagine a Situation In the Future.

A good way to figure out forbidden moves in a very complex situation is also to move them step by step. Let's make move to ``h9``, the point that we want to figure out.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/pseudo-forbid-complex-s1.png)

At first glance, ``i8`` seems to be 3-3 forbid where two stones placed vertically in column i and two stones placed horizontally in row 8. So black can't make straight four diagonally, so isn't ``g10`` a forbidden point?

That said, the three stones arranged vertically in a column f seem odd. It's too early to judge. Let's make one more move for ``i8``.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/pseudo-forbid-complex-s2.png)

After making a move on ``i8``, Black is blocked by 4-4 forbid in row 8 and cannot make a straight four. The ``i8`` after making one move in ``g10`` was not 3-3 forbid!

Now we can figure out that ``i8`` after moving to ``g10`` is not a forbidden point. Therefore, ``g10``, which can make two straight fours by moving black once, can figure out that 3-3 forbid is correct.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/pseudo-forbid-complex-s3.png)

In the same way, we can figure out that ``i10`` is also a 3-3 forbid. (The ``i10`` is a little more complicated.) Slowly, if you think about it one by one, it's not difficult.

## White Can Attack By Targeting Forbidden Points. {#forbid-trap}

Renju's forbidden moves are just a limitation for black, but for white, it's a strategy and an opportunity. Re-focus on the definition of forbidden moves. Black cannot be placed in a forbidden point in any case except five-in-a-row. Even if White can move to the forbidden point and win, it can't be an exception.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/forbid-trap-simple.png)

Here's an interesting situation. Black has one 3-3 forbid, and white has 3 stones lined up with 3-3 forbid in between, so white is ready for the four-attack. If white attacks by making four with the black forbidden point, how can black defend itself?

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/forbid-trap-simple-s1.png)

In order to remove 3-3 forbid, Black must make one move and remove one three. But it's too late for that. Very unfortunately, Black has no way to block white from attacking four. Black should be watching white win by creating a five-in-a-row.

## White Can Win by Creating a Forbidden Move Trap.

Now, if white attacks with a forbidden point, we know that black is going to have to watch white win. But you can't rely on luck or mistakes forever. Given the right circumstances, aggressive attacks can lead the black to create a forbidden point and win.

Here's a situation that looks awful for White. black attacked by making a three with ``f6``. At first glance, white seems to have to block black's three. The black stones that line lower-left also look very strong. Should white be attacked and defeated by black?

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/forbid-trap-complex.png)

Let's take a closer look at the situation again. White has 3 stones arranged diagonally and 3 stones arranged horizontally. Four attacks could go on twice. But it's like having a fit.

What we need to note in this situation is that white can create four and make black move to ``g9``, and if the black moves to ``g9``, ``g8`` becomes 3-3 forbid.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/forbid-trap-complex-s1.png)

*If white makes four, black must block four.*

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/forbid-trap-complex-s2.png)

Black was able to defend four, but two stones arranged horizontally in row 8 and two stones arranged vertically in column g resulted in 3-3 forbid.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/forbid-trap-complex-s3.png)

*Since ``g8`` is a 3-3 forbid, black has no way to stop the subsequent diagonal four attacks of white. White wins!*

The situation has completely changed! White can use 4-4 forbid and overline forbid in the same way to create and win traps. Black should read the situation well and be extra careful not to fall into the trap and ruin the game.

## Black Can Escape the Trap by Making its Non-Forbidden Move

Here's a twist. Black was able to win without being trapped. ``f6`` is an excellent point for black. But as we've seen before, if black move to ``f6``, Black will be caught in white's trap and defeated. How can black move safely to ``f6``?

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/counter-forbid-trap.png)

Here we need to re-imagine the conditions of forbidden moves. If three or four is not valid, then forbidden moves are also invalid. The black can remove forbidden point that will be generated by moving to ``g9`` in advance.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/counter-forbid-trap-s1.png)

*White must defend black's four.*

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/counter-forbid-trap-s2.png)

That's it! Now black can move freely with ``f6``. Black has made a place in advance to make a diagonal four, so three of the two horizontal and vertical threes that will be made as White's trap is no longer three.

![](https://raw.githubusercontent.com/junghyun397/GomokuBot/master/images/counter-forbid-trap-s4.png)

Although not always possible (this is a very rare situation in real games), Black can get out of the trap by making a "prevention move" that removes the condition of the forbidden moves. Well worth one last thought before giving up!

## Perfect!

Now you know everything you need to know about Renju. You know what's wrong with Gomoku, and you know how Renju solved Gomoku's problem. You know how to apply the Renju rule correctly in very complex situations, and you know how to use Renju to attack and defend.

Renju is a straightforward but very engaging game with an interesting and endless strategy. With GomokuBot, challenge more complex problems with your friends. It will definitely be fun.

If you have any questions, please feel free to visit the GomokuBot [support channel](https://discord.gg/vq8pkfF). I hope you have a good time with GomokuBot. — *GomokuBot developer junghyun397.*

## Taraguchi-10 {#taraguchi-10}

1. The black player makes the first move in the center of the board.
2. The white player may swap\*.
3. The white player makes the 2nd move within 3x3 central square.
4. The black player may swap.
5. The black player makes the 3rd move within 5x5 central square.
6. The white player may swap.
7. The white player makes the 4th move within 7x7 central square.
8. The black player can choose one or other:
   1. Swap.
      1. The black player may swap.
      2. The black player makes the 5th move within 9x9 central square.
      3. The white player may swap.
      4. The white player makes the 6th move anywhere on board.
   2. Offer.
      1. The black player picks ten 5th move candidates from anywhere on board. Symmetrical moves\*\* aren't allowed.
      2. The white player selects one of the offered 5th moves.
      3. The white player makes the 6th move anywhere on board.

\***Swap**: Both players swap black and white. If you swap, you pass the turn to opponent; if you don't swap, the next turn is yours.

\*\***Symmetrical moves**: A point that has the same shape when rotated or transposed.

## Soosyrv-8 {#soosyrv-8}

1. The black player makes the first move in the center of the board.
2. The black player makes the 2nd move within 3x3 central square.
3. The black player makes the 3rd move within 5x5 central square.
4. The white player may swap\*.
5. The white player makes the 4th move anywhere on board.
6. The white player declares the number of 5th move candidates. It must be declared between 1 and 8.
7. The black player may swap.
8. The black player picks the 5th move candidates from anywhere on board, as many as the white player declares. Symmetrical moves\*\* aren't allowed.
9. The white player selects one of the offered 5th moves.
10. The white player makes the 6th move anywhere on board.

\***Swap**: Both players swap black and white. If you swap, you pass the turn to opponent; if you don't swap, the next turn is yours.

\*\***Symmetrical moves**: A point that has the same shape when rotated or transposed.
""".trimIndent()
}