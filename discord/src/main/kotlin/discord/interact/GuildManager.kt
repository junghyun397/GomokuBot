package discord.interact

import core.assets.MessageRef
import core.assets.aiUser
import core.assets.anonymousUser
import core.interact.i18n.Language
import core.interact.i18n.LanguageContainer
import core.session.ArchivePolicy
import core.session.GameResult
import core.session.entities.AiGameSession
import core.session.entities.GameSession
import core.session.entities.PvpGameSession
import dev.minn.jda.ktx.interactions.commands.slash
import dev.minn.jda.ktx.interactions.commands.updateCommands
import discord.assets.JDAGuild
import discord.assets.awaitOption
import discord.interact.message.DiscordMessageData
import discord.interact.message.DiscordMessageProducer
import discord.interact.message.DiscordMessagePublisher
import discord.interact.message.MessageCreateAdaptor
import discord.interact.parse.buildableCommands
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.requests.RestAction
import utils.structs.Option
import utils.structs.getOrNull
import utils.structs.map
import utils.structs.orElseGet

object GuildManager {

    fun lookupPermission(channel: GuildChannel, permission: Permission) =
        channel.guild.selfMember.hasPermission(channel, permission)

    suspend fun hasDebugPermission(config: DiscordConfig, user: User): Boolean =
        user.jda
            .getGuildById(config.officialServerId.idLong)!!
            .retrieveMemberById(user.idLong)
            .awaitOption()
            .map { member -> member.roles.any { it.idLong == config.testerRoleId } }
            .orElseGet { false }

    inline fun <T> permissionGrantedRun(channel: TextChannel, permission: Permission, block: () -> T): Option<T> =
        if (this.lookupPermission(channel, permission)) Option(block())
        else Option.Empty

    inline fun <T> permissionDependedRun(channel: TextChannel, permission: Permission, onGranted: () -> T, onMissed: () -> T): T =
        if (this.lookupPermission(channel, permission)) onGranted()
        else onMissed()

    fun initGlobalCommand(jda: JDA) {
        jda.updateCommands {
            slash(Language.ENG.container.helpCommand(), Language.ENG.container.helpCommandDescription())
        }.queue()
    }

    fun upsertCommands(guild: JDAGuild, container: LanguageContainer) {
        buildableCommands.fold(guild.updateCommands()) { action, command ->
            command.buildCommandData(action, container)
        }.queue()
    }

    private fun core.assets.User.switchToAnonymousUser() =
        when (this) {
            aiUser -> aiUser
            else -> anonymousUser
        }

    suspend fun archiveSession(archiveChannel: TextChannel, session: GameSession, archivePolicy: ArchivePolicy) {
        if (session.board.moves() < 20 || archivePolicy == ArchivePolicy.PRIVACY) return

        val modSession = when (archivePolicy) {
            ArchivePolicy.BY_ANONYMOUS -> when (session) {
                is AiGameSession -> session.copy(owner = anonymousUser)
                is PvpGameSession -> session.copy(owner = anonymousUser, opponent = anonymousUser)
            }
            else -> session
        }

        val modResult = when (archivePolicy) {
            ArchivePolicy.BY_ANONYMOUS -> modSession.gameResult.map { result ->
                when (result) {
                    is GameResult.Win -> result.copy(
                        winner = result.winner.switchToAnonymousUser(),
                        loser = result.loser.switchToAnonymousUser()
                    )
                    else -> result
                }
            }
            else -> modSession.gameResult
        }

        val publisher: DiscordMessagePublisher = { msg -> MessageCreateAdaptor(archiveChannel.sendMessage(msg.buildCreate())) }

        DiscordMessageProducer.produceSessionArchive(publisher, modSession, modResult)
            .launch()
            .run()
    }

    suspend fun retrieveJDAMessage(jda: JDA, messageRef: MessageRef): net.dv8tion.jda.api.entities.Message? =
        jda.getGuildById(messageRef.guildId.idLong)
            ?.getTextChannelById(messageRef.channelId.idLong)
            ?.retrieveMessageById(messageRef.id.idLong)
            ?.awaitOption()
            ?.getOrNull()

    fun bulkDelete(jdaGuild: JDAGuild, messageRefs: List<MessageRef>) {
        if (messageRefs.isEmpty()) return

        messageRefs
            .groupBy { it.channelId }
            .flatMap { (channelId, messageRefs) ->
                when (val channel = jdaGuild.getTextChannelById(channelId.idLong)) {
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

    fun deleteSingle(jdaGuild: JDAGuild, messageRef: MessageRef) {
        val maybeChannel = jdaGuild.getTextChannelById(messageRef.channelId.idLong)

        maybeChannel?.deleteMessageById(messageRef.id.idLong)?.queue()
    }

    fun retainFirstEmbed(message: DiscordMessageData): DiscordMessageData =
        message.copy(embeds = message.embeds.subList(0, 1))

    fun clearComponents(message: DiscordMessageData): DiscordMessageData =
        message.copy(components = emptyList())

    fun clearReaction(message: net.dv8tion.jda.api.entities.Message) {
        this.permissionDependedRun(
            message.channel.asTextChannel(), Permission.MESSAGE_MANAGE,
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
