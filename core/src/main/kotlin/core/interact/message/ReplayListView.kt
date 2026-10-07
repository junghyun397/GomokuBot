package core.interact.message

import core.assets.User
import core.database.entities.GameRecord
import core.database.entities.GameRecordId
import core.interact.i18n.LanguageContainer
import core.session.entities.Rule
import renju.notation.Color
import renju.notation.map
import kotlin.time.Instant

data class ReplayListView(
    val container: LanguageContainer,
    val player: User.Human,
    val entries: List<ReplayEntry>,
)

data class ReplayEntry(
    val id: GameRecordId,
    val opponent: User,
    val playerColor: Color,
    val winner: Color?,
    val date: Instant,
    val rule: Rule,
    val moves: Int,
)

fun GameRecord.buildReplayEntry(player: User.Human): ReplayEntry {
    val color = this.users.map { it.id }.color(player.id)!!
    return ReplayEntry(
        id = this.gameRecordId!!,
        opponent = this.users[!color],
        playerColor = color,
        winner = this.gameResult.winner,
        date = this.date,
        rule = this.rule,
        moves = this.history.size,
    )
}
