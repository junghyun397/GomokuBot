package discord.assets

import core.assets.*
import net.dv8tion.jda.api.entities.Emoji

const val COMMAND_PREFIX = '~'

val EMOJI_BLACK_CIRCLE = Emoji.fromUnicode(UNICODE_BLACK_CIRCLE)
val EMOJI_WHITE_CIRCLE = Emoji.fromUnicode(UNICODE_WHITE_CIRCLE)
val EMOJI_DARK_X = Emoji.fromUnicode(UNICODE_DARK_X)

val EMOJI_ZAP = Emoji.fromUnicode(UNICODE_ZAP)
val EMOJI_MAG = Emoji.fromUnicode(UNICODE_MAG)

val EMOJI_BROOM = Emoji.fromUnicode(UNICODE_BROOM)
val EMOJI_CABINET = Emoji.fromUnicode(UNICODE_CABINET)

val EMOJI_SILHOUETTE = Emoji.fromUnicode(UNICODE_SILHOUETTE)
val EMOJI_SMILING = Emoji.fromUnicode(UNICODE_SMILING)
val EMOJI_LOCK = Emoji.fromUnicode(UNICODE_LOCK)

val EMOJI_IMAGE = Emoji.fromUnicode(UNICODE_IMAGE)
val EMOJI_T = Emoji.fromUnicode(UNICODE_T)
val EMOJI_GEM = Emoji.fromUnicode(UNICODE_GEM)

val ASCII_LOGO = """
                                                        ____________
                                                       /            \
                                                      / /          \ \
                                                     / /            \ \
                                                    / /              \ \
            __________  __  _______  __ ____  ______  ____  ______     /
           / ____/ __ \/  |/  / __ \/ //_/ / / / __ )/ __ \/_  __/    /
          / / __/ / / / /|_/ / / / / ,< / / / / __  / / / / / / ___  /
         / /_/ / /_/ / /  / / /_/ / /| / /_/ / /_/ / /_/ / / / _____/
        / /_/ / /_/ / /  / / /_/ / /| / /_/ / /_/ / /_/ / / /
        \____/\____/_/  /_/\____/_/ |_\____/_____/\____/ /_/
        
        Powered by [Kotlin, Project Reactor, R2DBC, PostgreSQL, gRPC, JDA]
""".trimIndent()
