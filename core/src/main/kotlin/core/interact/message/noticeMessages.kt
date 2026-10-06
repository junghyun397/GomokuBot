package core.interact.message

import core.assets.UNICODE_ALARM_CLOCK
import core.assets.UNICODE_SPEAKER
import core.assets.User
import core.database.entities.Announce
import core.engine.EngineLevel
import core.interact.i18n.LanguageContainer
import core.session.entities.*
import renju.notation.Color
import renju.notation.ColorContainer
import renju.notation.GameResult
import java.time.format.DateTimeFormatter

fun announcementMessage(container: LanguageContainer, announce: Announce) = AppMessage.Announcement(
    title = "$UNICODE_SPEAKER ${announce.title}",
    content = announce.content,
    publishedOn = container.announceWrittenOn("$UNICODE_ALARM_CLOCK ${announce.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm"))} UTC"),
)

fun requestTitle(container: LanguageContainer, session: RequestSession): String = when (session) {
    is RequestSession.Match -> container.requestEmbedTitle
    is RequestSession.Undo -> container.undoRequestEmbedTitle
}

fun requestDescription(container: LanguageContainer, session: RequestSession, requester: String, opponent: String): String = when (session) {
    is RequestSession.Match -> container.requestEmbedDescription(requester, opponent)
    is RequestSession.Undo -> container.undoRequestEmbedDescription(requester, opponent)
}

fun rejectedRequestMessage(container: LanguageContainer, session: RequestSession, requester: String, opponent: String) = AppMessage.Embed(
    level = NoticeLevel.ERROR,
    title = "~~${requestTitle(container, session)}~~",
    description = "~~${requestDescription(container, session, requester, opponent)}~~",
)

private fun EngineLevel.formatPlayerName(container: LanguageContainer, mention: String): String =
    "$mention ${container.engineLevel(this)}(${this.rating.rating})"

fun GameSession.formatPlayers(container: LanguageContainer, service: PlatformService): ColorContainer<String> =
    this.users.map { user ->
        if (user is User.GomokuBot && this is EngineGameSession)
            this.engineLevel.formatPlayerName(container, service.formatUser(user))
        else service.formatUser(user)
    }

fun gameStartedMessage(container: LanguageContainer, service: PlatformService, session: GameSession): AppMessage.GameStarted {
    val players = session.formatPlayers(container, service)
    val description = when (session) {
        is PvpGameSession -> container.beginPvp(players)
        is OpeningSession -> container.beginOpening(players)
        is EngineGameSession -> when (session.userColor) {
            Color.BLACK -> container.beginEngineWhite(players.black, players.white)
            Color.WHITE -> container.beginEngineBlack(players.white, players.black)
        }
    }

    return AppMessage.GameStarted(
        container = container,
        users = session.users,
        leaderColor = session.state.board.playerColor,
        description = description,
        rule = session.rule,
        enginePlayer = if (session is EngineGameSession) players[!session.userColor] else null,
    )
}

fun gameFinishedMessage(
    container: LanguageContainer,
    players: ColorContainer<String>,
    draw: ResultDraw,
): AppMessage.GameFinished {
    val description = when (val result = draw.result) {
        is GameResult.Win -> {
            val winner = players[result.winner]
            val loser = players[!result.winner]
            when (result.cause) {
                GameResult.WinCause.FIVE_IN_A_ROW -> container.gameResultFiveInRow(winner, loser)
                GameResult.WinCause.RESIGN -> container.gameResultResign(winner, loser)
                GameResult.WinCause.TIMEOUT -> container.gameResultTimeout(winner, loser)
            }
        }
        is GameResult.Full -> "${container.gameResultDraw}\n${players.black} vs ${players.white}"
    }
    return AppMessage.GameFinished(container, description, draw)
}
