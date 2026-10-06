package core.database

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import core.assets.*
import core.database.entities.Announce
import core.engine.EloRating
import core.interact.i18n.Language
import core.session.entities.ChannelConfig
import java.time.Duration
import java.util.*

data class LocalCaches(
    val channelConfigCache: Cache<ChannelUid, ChannelConfig> = CacheBuilder
        .newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(Duration.ofDays(1))
        .build(),

    val channelProfileUidCache: Cache<ChannelUid, Channel> = CacheBuilder
        .newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(Duration.ofDays(1))
        .build(),

    val userProfileUidCache: Cache<UserUid, User.Human> = CacheBuilder
        .newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(Duration.ofHours(6))
        .build(),

    val channelProfileGivenIdCache: Cache<ChannelId, Channel> = CacheBuilder
        .newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(Duration.ofDays(1))
        .build(),

    val userProfileGivenIdCache: Cache<UserId, User.Human> = CacheBuilder
        .newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(Duration.ofHours(6))
        .build(),

    val userRatingCache: Cache<UserUid, EloRating> = CacheBuilder
        .newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(Duration.ofHours(6))
        .build(),

    var announceCache: SortedMap<Int, Map<Language, Announce>> = sortedMapOf()
)
