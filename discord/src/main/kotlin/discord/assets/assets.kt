package discord.assets

import core.assets.*
import net.dv8tion.jda.api.entities.Emoji

const val COMMAND_PREFIX = 126.toChar() // "~"

val EMOJI_BLACK_CIRCLE = Emoji.fromUnicode(UNICODE_BLACK_CIRCLE)
val EMOJI_WHITE_CIRCLE = Emoji.fromUnicode(UNICODE_WHITE_CIRCLE)
val EMOJI_DARK_X = Emoji.fromUnicode(UNICODE_DARK_X)

val EMOJI_ZAP = Emoji.fromUnicode(UNICODE_ZAP)
val EMOJI_MAG = Emoji.fromUnicode(UNICODE_MAG)

val EMOJI_BROOM = Emoji.fromUnicode(UNICODE_BROOM)
val EMOJI_CABINET = Emoji.fromUnicode(UNICODE_CABINET)

val EMOJI_PERFORMING = Emoji.fromUnicode(UNICODE_PERFORMING)
val EMOJI_SILHOUETTE = Emoji.fromUnicode(UNICODE_SILHOUETTE)
val EMOJI_LOCK = Emoji.fromUnicode(UNICODE_LOCK)

const val ASCII_LOGO = "\n" +
        "                                                ____________\n" +
        "                                               /            \\\n" +
        "                                              / /          \\ \\\n" +
        "                                             / /            \\ \\\n" +
        "                                            / /              \\ \\\n" +
        "    __________  __  _______  __ ____  ______  ____  ______     /\n" +
        "   / ____/ __ \\/  |/  / __ \\/ //_/ / / / __ )/ __ \\/_  __/    /\n" +
        "  / / __/ / / / /|_/ / / / / ,< / / / / __  / / / / / / ___  /\n" +
        " / /_/ / /_/ / /  / / /_/ / /| / /_/ / /_/ / /_/ / / / _____/\n" +
        "/ /_/ / /_/ / /  / / /_/ / /| / /_/ / /_/ / /_/ / / /\n" +
        "\\____/\\____/_/  /_/\\____/_/ |_\\____/_____/\\____/ /_/\n" +
        "\n" +
        "Powered by [Kotlin, Project Reactor, R2DBC, MySQL, gRPC, JDA]"
