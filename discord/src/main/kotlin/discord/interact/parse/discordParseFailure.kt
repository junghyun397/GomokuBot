package discord.interact.parse

import core.interact.parse.ParseFailure
import discord.interact.message.DiscordButtons
import net.dv8tion.jda.api.entities.Message

typealias DiscordParseFailure = ParseFailure<Message, DiscordButtons>
