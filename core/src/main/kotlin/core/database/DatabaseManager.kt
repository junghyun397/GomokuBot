package core.database

import core.assets.*
import core.database.entities.GameRecord
import core.database.entities.UserStats
import core.interact.i18n.Language
import core.interact.message.graphics.BoardStyle
import core.session.ArchivePolicy
import core.session.FocusPolicy
import core.session.SweepPolicy
import core.session.entities.GuildConfig
import kotlinx.coroutines.reactive.awaitSingle
import utils.structs.Option
import utils.structs.Option.Empty.getOrElse
import utils.structs.asOption
import java.util.*

object DatabaseManager {

    suspend fun initTables(connection: DatabaseConnection) {
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createBatch()
                .add(
                    "CREATE TABLE IF NOT EXISTS guild_profile ()"
                )
                .add(
                    "CREATE TABLE IF NOT EXISTS guild_config ()"
                )
                .add(
                    "CREATE TABLE IF NOT EXISTS user_profile ()"
                )
                .add(
                    "CREATE TABLE IF NOT EXISTS user_stats ()"
                )
                .execute()
            }
            .awaitSingle()
    }

    // guild_profile

    suspend fun retrieveOrInsertGuild(connection: DatabaseConnection, platform: Int, givenId: GuildId, produce: () -> Guild): Guild =
        this.retrieveGuild(connection, platform, givenId)
            .getOrElse {
                produce()
                    .also { this.upsertGuild(connection, it) }
            }

    suspend fun retrieveGuild(connection: DatabaseConnection, platform: Int, givenId: GuildId): Option<Guild> =
        connection.caches.guildProfileCache
            .getIfPresent(givenId)
            .asOption()
            .flatMap { this.fetchGuild(connection, platform, givenId) }

    suspend fun retrieveGuild(connection: DatabaseConnection, guildUid: GuildUid): Guild =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement("a") // TODO()
                .bind("$1", guildUid.uuid)
                .execute()
            }
            .flatMap { result -> result
                .map { row, _ ->
                    Guild(
                        id = GuildUid(row["id"] as UUID),
                        platform = row["platform"] as Int,
                        givenId = GuildId(row["given_id"] as Long),
                        name = row["name"] as String
                    )
                }
            }
            .doOnNext { connection.caches.guildProfileCache.put(it.givenId, it) }
            .awaitSingle()

    private suspend fun fetchGuild(connection: DatabaseConnection, platform: Int, givenId: GuildId): Option<Guild> =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement("a") // TODO
                .execute()
            }
            .flatMap { result -> result
                .map { row, _ ->
                    Option(Guild(
                        id = GuildUid(row["id"] as UUID),
                        platform = platform,
                        givenId = givenId,
                        name = row["name"] as String
                    ))
                }
            }
            .switchIfEmpty { Option.Empty }
            .awaitSingle()

    fun upsertGuild(connection: DatabaseConnection, guild: Guild) {
        connection.caches.guildProfileCache.put(guild.givenId, guild)

        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement("a") // TODO
                .execute()
            }
            .subscribe()
    }

    // guild_config

    suspend fun fetchGuildConfig(connection: DatabaseConnection, guildUid: GuildUid): Option<GuildConfig> =
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement(
                    "SELECT * FROM guild_info WHERE id == $1"
                )
                .bind("$1", guildUid.uuid)
                .execute()
            }
            .flatMap { result -> result
                .map { row, _ ->
                    Option(GuildConfig(
                        language = Language.values().find { it.id == row["language"] as Int }!!,
                        boardStyle = BoardStyle.values().find { it.id == row["board_style"] as Int }!!,
                        focusPolicy = FocusPolicy.values().find { it.id == row["focus_policy"] as Int }!!,
                        sweepPolicy = SweepPolicy.values().find { it.id == row["sweep_policy"] as Int }!!,
                        archivePolicy = ArchivePolicy.values().find { it.id == row["archive_policy"] as Int }!!
                    ))
                }
            }
            .switchIfEmpty { Option.Empty }
            .awaitSingle()

    fun upsertGuildConfig(connection: DatabaseConnection, guildUid: GuildUid, guildConfig: GuildConfig) {
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement(
                    "UPDATE guild_info SET language=$1, board_style=$2, focus_policy=$3, sweep_policy=$4, archive_policy=$5 WHERE id == $6"
                )
                .bind("$1", guildConfig.language.id)
                .bind("$2", guildConfig.boardStyle.id)
                .bind("$3", guildConfig.focusPolicy.id)
                .bind("$4", guildConfig.sweepPolicy.id)
                .bind("$5", guildConfig.archivePolicy.id)
                .bind("$6", guildUid.uuid)
                .execute()
            }
            .subscribe()
    }

    // user_profile

    suspend fun retrieveOrInsertUser(connection: DatabaseConnection, platform: Int, givenId: UserId, produce: () -> User): User =
        this.retrieveUser(connection, platform, givenId)
            .getOrElse {
                produce()
                    .also { this.upsertUser(connection, it) }
            }

    suspend fun retrieveUser(connection: DatabaseConnection, platform: Int, givenId: UserId): Option<User> = TODO()

    suspend fun fetchUser(connection: DatabaseConnection, platform: Int, givenId: UserId): Option<User> = TODO()

    fun upsertUser(connection: DatabaseConnection, user: User): Unit = TODO()

    // user_stats

    suspend fun retrieveUserStats(connection: DatabaseConnection, userUid: UserUid): Option<UserStats> = TODO()

    suspend fun fetchUserStats(connection: DatabaseConnection, userUid: UserUid): Option<UserStats> = TODO()

    fun upsertUserStats(connection: DatabaseConnection, userStats: UserStats): Unit = TODO()

    suspend fun fetchRankings(connection: DatabaseConnection): List<UserStats> = TODO()

    // game_record

    suspend fun uploadGameRecord(connection: DatabaseConnection, record: GameRecord) {
        connection.liftConnection()
            .flatMapMany { dbc -> dbc
                .createStatement("a")
                .execute()
            }
            .subscribe()
    }

}
