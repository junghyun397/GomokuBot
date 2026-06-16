package discord.interact

import arrow.core.raise.get
import core.assets.ChannelUid
import core.assets.MessageRef
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.interact.message.SessionBoardDraw
import core.session.entities.ArchivePolicy
import core.session.entities.GameSession
import dev.minn.jda.ktx.coroutines.await
import discord.assets.JDAChannel
import discord.assets.awaitNullable
import discord.assets.subChannelById
import discord.interact.message.DiscordMessagePublisher
import discord.interact.message.DiscordMessagingService
import discord.interact.message.MessageCreateAdaptor
import discord.interact.message.asDiscordMessageData
import discord.interact.parse.BuildableCommand
import discord.interact.parse.buildableCommands
import discord.interact.parse.engBuildableCommands
import discord.interact.parse.parsers.HelpCommandParser
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.requests.RestAction
import utils.memoize
import utils.replaceIf
import utils.tuple

object ChannelManager {

    val updateCommandBypassChannels = mutableSetOf<ChannelUid>()

    fun lookupPermission(channel: GuildMessageChannel, permission: Permission) =
        channel.guild.selfMember.hasPermission(channel, permission)

    inline fun <T> permissionGrantedRun(channel: GuildMessageChannel, permission: Permission, block: () -> T): T? =
        if (this.lookupPermission(channel, permission)) block()
        else null

    inline fun <T> permissionDependedRun(channel: GuildMessageChannel, permission: Permission, onGranted: () -> T, onMissed: () -> T): T =
        if (this.lookupPermission(channel, permission)) onGranted()
        else onMissed()

    fun initGlobalCommand(jda: JDA) {
        HelpCommandParser.buildHelpCommandData(jda.updateCommands(), Language.ENG.container).queue()
    }

    private val buildableCommandIndexes: (LanguageContainer) -> Map<String, BuildableCommand> = memoize { container ->
        buildableCommands.associateBy { it.getLocalizedName(container) }
    }

    private val engBuildableCommandIndexes: Map<String, BuildableCommand> =
        engBuildableCommands.associateBy { it.getLocalizedName(Language.ENG.container) }

    suspend fun buildCommandUpdates(guild: JDAChannel, container: LanguageContainer): Pair<List<Command>, List<BuildableCommand>> =
        guild.retrieveCommands()
            .map { commands ->
                val localCommands = commands.toSet()

                val serverCommands = buildableCommandIndexes(container)
                    .replaceIf(container == Language.ENG.container) { engBuildableCommandIndexes }

                val deprecates = localCommands
                    .filterNot { command -> serverCommands.containsKey(command.name) }

                val adds = serverCommands
                    .filterKeys { name -> !localCommands.any { command -> command.name == name } }
                    .values
                    .toList()

                tuple(deprecates, adds)
            }
            .await()

    fun upsertCommands(jdaChannel: JDAChannel, container: LanguageContainer) {
        buildableCommands.fold(jdaChannel.updateCommands()) { action, command ->
            command.buildCommandData(action, container)
        }.queue()
    }

    suspend fun archiveSession(archiveSubChannel: MessageChannel, session: GameSession, archivePolicy: ArchivePolicy) {
        if (session.state.history.size < 20 || archivePolicy == ArchivePolicy.PRIVACY) return

        val publisher: DiscordMessagePublisher = { msg -> MessageCreateAdaptor(archiveSubChannel.sendMessage(msg.asDiscordMessageData().buildCreate())) }

        DiscordMessagingService.buildSessionArchive(publisher, SessionBoardDraw(
            session,
            anonymous = archivePolicy == ArchivePolicy.BY_ANONYMOUS
        ))
            .launch()
            .get()
    }

    suspend fun retrieveJDAMessage(jda: JDA, messageRef: MessageRef): net.dv8tion.jda.api.entities.Message? =
        jda.getGuildById(messageRef.channelId.idLong)
            ?.getTextChannelById(messageRef.subChannelId.idLong)
            ?.retrieveMessageById(messageRef.id.idLong)
            ?.awaitNullable()

    fun bulkDelete(jdaChannel: JDAChannel, messageRefs: List<MessageRef>) {
        if (messageRefs.isEmpty()) return

        messageRefs
            .groupBy { it.subChannelId }
            .flatMap { (subChannelId, messageRefs) ->
                when (val channel = jdaChannel.subChannelById(subChannelId.idLong)) {
                    null -> emptyList<RestAction<*>>()
                    else -> {
                        this.permissionDependedRun(
                            channel, Permission.MESSAGE_MANAGE,
                            onMissed = {
                                messageRefs
                                    .map { channel.deleteMessageById(it.id.idLong) }
                            },
                            onGranted = {
                                messageRefs
                                    .chunked(100)
                                    .map { messageRefs ->
                                        when (messageRefs.size) {
                                            1 -> channel.deleteMessageById(messageRefs.first().id.idLong)
                                            else -> channel.deleteMessagesByIds(messageRefs.map { it.id.idLong.toString() })
                                        }
                                    }
                            }
                        )
                    }
                }
            }
            .reduce { acc, restAction -> acc.and(restAction) }
            .queue()
    }

    fun deleteSingle(jdaChannel: JDAChannel, messageRef: MessageRef) {
        val maybeSubChannel = jdaChannel.getTextChannelById(messageRef.subChannelId.idLong)

        maybeSubChannel?.deleteMessageById(messageRef.id.idLong)?.queue()
    }

    fun clearReaction(message: net.dv8tion.jda.api.entities.Message) {
        this.permissionDependedRun(
            message.channel.asGuildMessageChannel(), Permission.MESSAGE_MANAGE,
            onMissed = {
                message.reactions
                    .map { it.removeReaction(message.jda.selfUser) }
                    .takeIf { it.isNotEmpty() }
                    ?.reduce { acc, action -> acc.and(action) }
            },
            onGranted = { message.clearReactions() }
        )?.queue()
    }

}
