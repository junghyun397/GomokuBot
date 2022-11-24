package discord.assets

import core.assets.*
import dev.minn.jda.ktx.coroutines.await
import discord.interact.message.MessageActionAdaptor
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.api.requests.RestAction
import utils.structs.Option
import java.util.*
import kotlin.reflect.KClass

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

fun net.dv8tion.jda.api.entities.Channel.extractId(): ChannelId = ChannelId(this.idLong)

fun net.dv8tion.jda.api.entities.Guild.extractProfile(uid: GuildUid = GuildUid(UUID.randomUUID())): Guild =
    Guild(uid, DISCORD_PLATFORM_ID, this.extractId(), this.name)

fun net.dv8tion.jda.api.entities.User.extractProfile(uid: UserUid = UserUid(UUID.randomUUID()), announceId: Int? = null): User =
    User(uid, DISCORD_PLATFORM_ID, this.extractId(), this.name, this.asTag, announceId, this.avatarUrl)

fun net.dv8tion.jda.api.entities.Guild.editMessageByMessageRef(ref: MessageRef, newContent: net.dv8tion.jda.api.entities.Message): MessageActionAdaptor =
    MessageActionAdaptor(this.getTextChannelById(ref.channelId.idLong)!!.editMessageById(ref.id.idLong, newContent))

suspend fun <T> RestAction<T>.awaitOption(): Option<T> = this
    .mapToResult()
    .map { Option.cond(it.isSuccess) { it.get() } }
    .await()

fun <T : Event> getEventAbbreviation(source: KClass<T>): String =
    when (source) {
        SlashCommandInteractionEvent::class -> "SCIE"
        MessageReceivedEvent::class -> "MRE"
        ButtonInteractionEvent::class -> "BIE"
        SelectMenuInteractionEvent::class -> "SMIE"
        MessageReactionAddEvent::class -> "MRAE"
        MessageReactionRemoveEvent::class -> "MRRE"
        GuildJoinEvent::class -> "GJE"
        GuildLeaveEvent::class -> "GLE"
        else -> "UNK"
    }
