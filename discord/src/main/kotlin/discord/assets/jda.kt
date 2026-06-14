package discord.assets

import core.assets.*
import dev.minn.jda.ktx.coroutines.await
import discord.interact.message.DiscordMessageBuilder
import discord.interact.message.DiscordMessageData
import discord.interact.message.MessageEditAdaptor
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

typealias JDAChannel = net.dv8tion.jda.api.entities.Guild

fun net.dv8tion.jda.api.entities.Message.messageRef(): MessageRef =
    MessageRef(this.messageId(), this.guild.channelId(), this.channel.subChannelId())

fun GenericMessageReactionEvent.messageRef(): MessageRef =
    MessageRef(MessageId(this.messageIdLong), this.guild.channelId(), this.channel.subChannelId())

fun net.dv8tion.jda.api.entities.Guild.channelId(): ChannelId = ChannelId(this.idLong)

fun net.dv8tion.jda.api.entities.User.userId(): UserId = UserId(this.idLong)

fun net.dv8tion.jda.api.entities.Message.messageId(): MessageId = MessageId(this.idLong)

fun net.dv8tion.jda.api.entities.channel.Channel.subChannelId(): SubChannelId = SubChannelId(this.idLong)

fun net.dv8tion.jda.api.entities.Guild.profile(uid: ChannelUid = ChannelUid(UUID.randomUUID())): Channel =
    Channel(uid, DISCORD_PLATFORM_ID, this.channelId(), this.name)

fun net.dv8tion.jda.api.entities.User.profile(uid: UserUid = UserUid(UUID.randomUUID()), announceId: Int? = null): User.Human =
    User.Human(this.effectiveName, this.avatarUrl, uid, DISCORD_PLATFORM_ID, this.userId(), this.name, announceId)

fun net.dv8tion.jda.api.entities.Guild.editMessageByMessageRef(ref: MessageRef, newContent: MessageEditData): DiscordMessageBuilder =
    MessageEditAdaptor(this.getChannelMessageSubChannelById(ref.subChannelId.idLong)!!.editMessageById(ref.id.idLong, newContent))

fun net.dv8tion.jda.api.entities.Message.messageData(): DiscordMessageData =
    DiscordMessageData(
        this.contentRaw,
        this.embeds,
        this.attachments,
        this.components,
        this.isTTS,
        this
    )

fun net.dv8tion.jda.api.entities.Guild.getChannelMessageSubChannelById(idLong: Long): GuildMessageChannel? =
    this.getTextChannelById(idLong)
        ?: let { this.getThreadChannelById(idLong) }
        ?: let { this.getVoiceChannelById(idLong) }

suspend fun <T> RestAction<T>.awaitNullable(): T? = this
    .mapToResult()
    .map { if (it.isSuccess) it.get() else null }
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
