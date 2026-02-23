package discord.interact

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import arrow.core.getOrElse
import arrow.core.raise.get
import core.assets.ChannelUid
import core.assets.MessageRef
import core.assets.aiUser
import core.assets.anonymousUser
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.session.ArchivePolicy
import core.session.entities.AiGameSession
import core.session.entities.PvpGameSession
import core.session.entities.RenjuSession
import dev.minn.jda.ktx.coroutines.await
import discord.assets.JDAChannel
import discord.assets.awaitOption
import discord.assets.getChannelMessageSubChannelById
import discord.interact.message.DiscordMessageData
import discord.interact.message.DiscordMessagePublisher
import discord.interact.message.DiscordMessagingService
import discord.interact.message.MessageCreateAdaptor
import discord.interact.parse.BuildableCommand
import discord.interact.parse.buildableCommands
import discord.interact.parse.engBuildableCommands
import discord.interact.parse.parsers.HelpCommandParser
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.requests.RestAction
import renju.notation.GameResult
import utils.lang.memoize
import utils.lang.replaceIf
import utils.lang.tuple

object ChannelManager {

    val updateCommandBypassChannels = mutableSetOf<ChannelUid>()

    fun lookupPermission(channel: GuildMessageChannel, permission: Permission) =
        channel.guild.selfMember.hasPermission(channel, permission)

    suspend fun hasDebugPermission(config: DiscordConfig, user: User): Boolean =
        user.jda
            .getGuildById(config.officialServerId.idLong)!!
            .retrieveMemberById(user.idLong)
            .awaitOption()
            .map { member -> member.roles.any { it.idLong == config.testerRoleId } }
            .getOrElse { false }

    inline fun <T> permissionGrantedRun(channel: GuildMessageChannel, permission: Permission, block: () -> T): Option<T> =
        if (this.lookupPermission(channel, permission)) Some(block())
        else None

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

    private fun core.assets.User.switchToAnonymousUser() =
        when (this) {
            aiUser -> aiUser
            else -> anonymousUser
        }

    suspend fun archiveSession(archiveSubChannel: MessageChannel, session: RenjuSession, archivePolicy: ArchivePolicy) {
        if (session.board.moves() < 20 || archivePolicy == ArchivePolicy.PRIVACY) return

        val modSession = session.replaceIf(archivePolicy == ArchivePolicy.BY_ANONYMOUS) {
            when (session) {
                is AiGameSession -> session.copy(owner = anonymousUser)
                is PvpGameSession -> session.copy(owner = anonymousUser, opponent = anonymousUser)
            }
        }

        val modResult = modSession.gameResult.map { result ->
            result.replaceIf(archivePolicy == ArchivePolicy.BY_ANONYMOUS) {
                when (result) {
                    is GameResult.Win -> result.copy(
                        winner = result.winner.switchToAnonymousUser(),
                        loser = result.loser.switchToAnonymousUser()
                    )
                    else -> result
                }
            }
        }

        val publisher: DiscordMessagePublisher = { msg -> MessageCreateAdaptor(archiveSubChannel.sendMessage(msg.buildCreate())) }

        DiscordMessagingService.buildSessionArchive(publisher, modSession, modResult, false)
            .launch()
            .get()
    }

    suspend fun retrieveJDAMessage(jda: JDA, messageRef: MessageRef): net.dv8tion.jda.api.entities.Message? =
        jda.getGuildById(messageRef.channelId.idLong)
            ?.getTextChannelById(messageRef.subChannelId.idLong)
            ?.retrieveMessageById(messageRef.id.idLong)
            ?.awaitOption()
            ?.getOrNull()

    fun bulkDelete(jdaChannel: JDAChannel, messageRefs: List<MessageRef>) {
        if (messageRefs.isEmpty()) return

        messageRefs
            .groupBy { it.subChannelId }
            .flatMap { (subChannelId, messageRefs) ->
                when (val channel = jdaChannel.getChannelMessageSubChannelById(subChannelId.idLong)) {
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

    fun DiscordMessageData.retainFirstEmbed(): DiscordMessageData =
        this.copy(embeds = this.embeds.firstOrNull()?.let { listOf(it) } ?: emptyList())

    fun DiscordMessageData.clearFiles(): DiscordMessageData =
        this.copy(files = emptyList())

    fun DiscordMessageData.clearComponents(): DiscordMessageData =
        this.copy(components = emptyList())

}
