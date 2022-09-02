package discord.interact.parse

import core.interact.parse.ParseFailure
import discord.interact.message.DiscordComponents
import net.dv8tion.jda.api.entities.Message

typealias DiscordParseFailure = ParseFailure<Message, DiscordComponents>
