package core.database

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import core.assets.*
import core.database.entities.Announce
import core.interact.i18n.Language
import java.util.*
import java.util.concurrent.TimeUnit

data class LocalCaches(
    val channelProfileUidCache: Cache<ChannelUid, Channel> = CacheBuilder
        .newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(1, TimeUnit.DAYS)
        .build(),

    val userProfileUidCache: Cache<UserUid, User> = CacheBuilder
        .newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(6, TimeUnit.HOURS)
        .build(),

    val channelProfileGivenIdCache: Cache<ChannelId, Channel> = CacheBuilder
        .newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(1, TimeUnit.DAYS)
        .build(),

    val userProfileGivenIdCache: Cache<UserId, User> = CacheBuilder
        .newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(6, TimeUnit.HOURS)
        .build(),

    var announceCache: SortedMap<Int, Map<Language, Announce>> = sortedMapOf()
)
