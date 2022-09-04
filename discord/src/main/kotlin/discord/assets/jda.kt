package discord.assets

import core.assets.*
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
import java.util.*
import kotlin.reflect.KClass

const val DISCORD_PLATFORM_ID: Short = 1

typealias JDAGuild = net.dv8tion.jda.api.entities.Guild

fun net.dv8tion.jda.api.entities.Message.extractMessageRef() =
    MessageRef(this.extractId(), this.guild.extractId(), this.channel.extractId())

fun GenericMessageReactionEvent.extractMessageRef() =
    MessageRef(MessageId(this.messageIdLong), this.guild.extractId(), this.channel.extractId())

fun net.dv8tion.jda.api.entities.Guild.extractId() = GuildId(this.idLong)

fun net.dv8tion.jda.api.entities.User.extractId() = UserId(this.idLong)

fun net.dv8tion.jda.api.entities.Message.extractId() = MessageId(this.idLong)

fun net.dv8tion.jda.api.entities.Channel.extractId() = ChannelId(this.idLong)

fun net.dv8tion.jda.api.entities.Guild.extractProfile(uid: GuildUid = GuildUid(UUID.randomUUID())) =
    Guild(uid, DISCORD_PLATFORM_ID, this.extractId(), this.name)

fun net.dv8tion.jda.api.entities.User.extractProfile(uid: UserUid = UserUid(UUID.randomUUID()), announceId: Int? = null) =
    User(uid, DISCORD_PLATFORM_ID, this.extractId(), this.name, this.asTag, announceId, this.avatarUrl)

fun <T : Event>getEventAbbreviation(source: KClass<T>) =
    when (source)  {
        SlashCommandInteractionEvent::class -> "SCE"
        MessageReceivedEvent::class -> "MRE"
        ButtonInteractionEvent::class -> "BIE"
        SelectMenuInteractionEvent::class -> "SME"
        MessageReactionAddEvent::class -> "MRA"
        MessageReactionRemoveEvent::class -> "MRR"
        GuildJoinEvent::class -> "GJE"
        GuildLeaveEvent::class -> "GLE"
        else -> "UNK"
    }
