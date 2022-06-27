package core.database

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import core.assets.Guild
import core.assets.GuildId
import core.assets.User
import core.assets.UserId
import java.time.Duration

data class Caches(
    val guildProfileCache: Cache<GuildId, Guild> = CacheBuilder
        .newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(Duration.ofDays(1))
        .build(),

    val userProfileCache: Cache<UserId, User> = CacheBuilder
        .newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(Duration.ofHours(6))
        .build()
)
