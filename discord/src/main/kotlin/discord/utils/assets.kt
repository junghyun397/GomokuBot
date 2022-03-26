package discord.utils

import net.dv8tion.jda.api.entities.Emoji
import utils.assets.UNICODE_BLACK_CIRCLE
import utils.assets.UNICODE_WHITE_CIRCLE

const val COMMAND_PREFIX = 126.toChar() // "~"

const val SELF_LENGTH = "GomokuBot#0000".length

val EMOJI_BLACK_CIRCLE = Emoji.fromUnicode(UNICODE_BLACK_CIRCLE)
val EMOJI_WHITE_CIRCLE = Emoji.fromUnicode(UNICODE_WHITE_CIRCLE)

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
