package core.interact.message

import core.assets.User
import core.database.entities.UserStats
import core.engine.EloRating
import core.interact.i18n.LanguageContainer
import core.session.entities.ChannelConfig
import core.session.entities.RequestSession

sealed interface AppMessage {

    data class Text(val content: String) : AppMessage

    data class Embed(
        val description: String,
        val title: String? = null,
        val level: NoticeLevel = NoticeLevel.INFO,
    ) : AppMessage

    data object LanguageGuide : AppMessage

    data class Announcement(val title: String, val content: String, val publishedOn: String) : AppMessage

    data class Rankings(val container: LanguageContainer, val entries: List<Pair<User, UserStats>>) : AppMessage

    data class Rating(val user: User, val rating: EloRating, val recentDelta: EloRating.Delta) : AppMessage

    data class GameFinished(val container: LanguageContainer, val description: String, val draw: ResultDraw) : AppMessage

    data class Board(val view: BoardView) : AppMessage

    data class BoardArchive(val draw: BoardDraw) : AppMessage

    data class Replay(val draw: BoardDraw) : AppMessage

    data class ReplayList(val view: ReplayListView) : AppMessage

    data class Help(val container: LanguageContainer, val page: Int) : AppMessage

    data class Settings(val config: ChannelConfig, val page: Int) : AppMessage

    data class Request(
        val container: LanguageContainer,
        val session: RequestSession,
    ) : AppMessage

}

enum class NoticeLevel {
    INFO, ERROR
}
