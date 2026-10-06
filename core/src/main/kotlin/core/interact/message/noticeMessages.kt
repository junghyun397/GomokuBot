package core.interact.message

import core.assets.UNICODE_ALARM_CLOCK
import core.assets.UNICODE_SPEAKER
import core.database.entities.Announce
import core.interact.i18n.LanguageContainer
import core.session.entities.RequestSession
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

fun gameFinishedMessage(container: LanguageContainer, service: PlatformService, draw: ResultDraw): AppMessage.GameFinished {
    val description = when (val result = draw.result) {
        is GameResult.Win -> {
            val winner = service.formatUser(draw.users[result.winner])
            val loser = service.formatUser(draw.users[!result.winner])
            when (result.cause) {
                GameResult.WinCause.FIVE_IN_A_ROW -> container.gameResultFiveInRow(winner, loser)
                GameResult.WinCause.RESIGN -> container.gameResultResign(winner, loser)
                GameResult.WinCause.TIMEOUT -> container.gameResultTimeout(winner, loser)
            }
        }
        is GameResult.Full -> container.gameResultDraw
    }
    return AppMessage.GameFinished(container, description, draw)
}
