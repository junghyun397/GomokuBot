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

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromType(ChannelType.TEXT)
                || (event.getMessage().getContentDisplay().length() < 4)
                || (event.getMessage().getContentDisplay().toCharArray()[0] != Settings.PREFIX)) return;
        BotManager.processCommand(event);
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        if (event.getGuild().getSystemChannel() == null) {
            MessageManager.getInstance(event.getGuild()).sendHelp(Objects.requireNonNull(event.getGuild().getSystemChannel()));
            MessageManager.getInstance(event.getGuild()).sendLanguageInfo(event.getGuild().getSystemChannel());
        }
        Logger.loggerInfo("join server : " + event.getGuild().getName());
    }

}
