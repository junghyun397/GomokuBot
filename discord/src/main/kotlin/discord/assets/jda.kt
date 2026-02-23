package discord.assets

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import core.assets.*
import core.interact.ExecutionContext
import dev.minn.jda.ktx.coroutines.await
import discord.interact.InteractionContext
import discord.interact.message.DiscordMessageBuilder
import discord.interact.message.DiscordMessageData
import discord.interact.message.MessageEditAdaptor
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.utils.messages.MessageEditData
import java.util.*

const val DISCORD_PLATFORM_ID: Short = 1

typealias JDAUser = net.dv8tion.jda.api.entities.User

typealias JDAGuild = net.dv8tion.jda.api.entities.Guild

fun net.dv8tion.jda.api.entities.Message.extractMessageRef(): MessageRef =
    MessageRef(this.extractId(), this.guild.extractId(), this.channel.extractId())

fun GenericMessageReactionEvent.extractMessageRef(): MessageRef =
    MessageRef(MessageId(this.messageIdLong), this.guild.extractId(), this.channel.extractId())

fun net.dv8tion.jda.api.entities.Guild.extractId(): GuildId = GuildId(this.idLong)

fun net.dv8tion.jda.api.entities.User.extractId(): UserId = UserId(this.idLong)

fun net.dv8tion.jda.api.entities.Message.extractId(): MessageId = MessageId(this.idLong)

fun net.dv8tion.jda.api.entities.channel.Channel.extractId(): ChannelId = ChannelId(this.idLong)

fun net.dv8tion.jda.api.entities.Guild.extractProfile(uid: GuildUid = GuildUid(UUID.randomUUID())): Guild =
    Guild(uid, DISCORD_PLATFORM_ID, this.extractId(), this.name)

fun net.dv8tion.jda.api.entities.User.extractProfile(uid: UserUid = UserUid(UUID.randomUUID()), announceId: Int? = null): User =
    User(uid, DISCORD_PLATFORM_ID, this.extractId(), this.effectiveName, this.name, announceId, this.avatarUrl)

fun net.dv8tion.jda.api.entities.Guild.editMessageByMessageRef(ref: MessageRef, newContent: MessageEditData): DiscordMessageBuilder =
    MessageEditAdaptor(this.getGuildMessageChannelById(ref.channelId.idLong)!!.editMessageById(ref.id.idLong, newContent))

fun net.dv8tion.jda.api.entities.Message.extractMessageData(): DiscordMessageData =
    DiscordMessageData(
        this.contentRaw,
        this.embeds,
        this.attachments,
        this.components,
        this.isTTS,
        Some(this)
    )

fun net.dv8tion.jda.api.entities.Guild.getGuildMessageChannelById(idLong: Long): GuildMessageChannel? =
    this.getTextChannelById(idLong)
        ?: let { this.getThreadChannelById(idLong) }
        ?: let { this.getVoiceChannelById(idLong) }

suspend fun <T> RestAction<T>.awaitOption(): Option<T> = this
    .mapToResult()
    .map { if (it.isSuccess) Some(it.get()) else None }
    .await()

fun <T : Event> T.abbreviation(): String =
    when (this::class) {
        SlashCommandInteractionEvent::class -> "SCIE"
        MessageReceivedEvent::class -> "MRE"
        ButtonInteractionEvent::class -> "BIE"
        EntitySelectInteractionEvent::class -> "ESIE"
        MessageReactionAddEvent::class -> "MRAE"
        MessageReactionRemoveEvent::class -> "MRRE"
        GuildJoinEvent::class -> "GJE"
        GuildLeaveEvent::class -> "GLE"
        else -> "UNK"
    }

fun ExecutionContext.retrieveJDAGuild(jda: JDA): JDAGuild =
    when (this) {
        is InteractionContext<*> -> this.jdaGuild
        else -> jda.getGuildById(this.guild.givenId.idLong)!!
    }
