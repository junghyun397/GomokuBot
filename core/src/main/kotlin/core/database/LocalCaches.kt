package core.database

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import core.assets.*
import core.database.entities.Announce
import core.interact.i18n.Language
import java.time.Duration
import java.util.*

data class LocalCaches(
    val guildProfileUidCache: Cache<GuildUid, Guild> = CacheBuilder
        .newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(Duration.ofDays(1))
        .build(),

    val userProfileUidCache: Cache<UserUid, User> = CacheBuilder
        .newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(Duration.ofHours(6))
        .build(),

    val guildProfileGivenIdCache: Cache<GuildId, Guild> = CacheBuilder
        .newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(Duration.ofDays(1))
        .build(),

    val userProfileGivenIdCache: Cache<UserId, User> = CacheBuilder
        .newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(Duration.ofHours(6))
        .build(),

    var announceCache: SortedMap<Int, Map<Language, Announce>> = sortedMapOf()
)
