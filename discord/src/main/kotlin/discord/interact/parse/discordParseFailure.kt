package discord.interact.parse

import core.interact.parse.ParseFailure
import discord.interact.message.DiscordComponents
import discord.interact.message.DiscordMessageData

typealias DiscordParseFailure = ParseFailure<DiscordMessageData, DiscordComponents>
