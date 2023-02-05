package discord.interact.parse

import core.interact.parse.ParseFailure
import discord.assets.DiscordMessageData
import discord.interact.message.DiscordComponents

typealias DiscordParseFailure = ParseFailure<DiscordMessageData, DiscordComponents>
