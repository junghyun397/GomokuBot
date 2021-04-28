package junghyun.discord;

import junghyun.discord.db.Logger;
import junghyun.discord.ui.MessageManager;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EventListener extends ListenerAdapter {

    private final BotManager botManager;
    private final Logger logger;
    private final MessageManager messageManager;

    public EventListener(BotManager botManager, Logger logger, MessageManager messageManager) {
        this.botManager = botManager;
        this.logger = logger;
        this.messageManager = messageManager;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromType(ChannelType.TEXT)
                || (event.getMessage().getContentDisplay().length() < 4)
                || (event.getMessage().getContentDisplay().toCharArray()[0] != Settings.PREFIX)) return;
        this.botManager.processCommand(event);
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        if (event.getGuild().getSystemChannel() != null
                && event.getGuild().getSystemChannel().canTalk()) {
            this.messageManager.getAgent(event.getGuild()).sendHelp(Objects.requireNonNull(event.getGuild().getSystemChannel()));
            this.messageManager.getAgent(event.getGuild()).sendSkinInfo(event.getGuild().getSystemChannel());
            this.messageManager.getAgent(event.getGuild()).sendLanguageInfo(event.getGuild().getSystemChannel());
        }

        this.logger.loggerInfo("join server : " + event.getGuild().getName());
    }

}
