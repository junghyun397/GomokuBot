package junghyun.discord;

import junghyun.ai.Pos;
import junghyun.discord.db.DBManager;
import junghyun.discord.db.Logger;
import junghyun.discord.db.SqlManager;
import junghyun.discord.ui.MessageManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;
import java.util.Objects;

public class BotManager {

    private static JDA client;

    public static void startGomokuBot() throws LoginException, InterruptedException {
        JDABuilder builder = new JDABuilder(Settings.TOKEN);
        builder.setDisabledCacheFlags(EnumSet.of(CacheFlag.ACTIVITY, CacheFlag.VOICE_STATE));
        builder.setBulkDeleteSplittingEnabled(false);
        builder.setActivity(Activity.watching("~help"));

        builder.addEventListeners(new EventListener());

        client = builder.build();
        client.awaitReady();

        SqlManager.connectMysql();
        GameManager.bootGameManager();
        MessageManager.loadMessage();
    }

    public static void endGomokuBot() {
        BotManager.client.shutdown();
    }

    static void processCommand(MessageReceivedEvent event) {
        if (event.getMessage().getContentDisplay().isEmpty()
                || event.getAuthor().isFake()
                || !event.getTextChannel().canTalk())
            return;

        String[] splitText = event.getMessage().getContentDisplay().toLowerCase().split(" ");
        Logger.loggerCommand(event.getAuthor().getName() + " : " + event.getAuthor().getName()
                + " / " + event.getMessage().getContentDisplay());

        switch (splitText[0]) {
            case "~help":
                MessageManager.getInstance(event.getGuild()).sendHelp(event.getTextChannel());
                break;
            case "~lang":
                if (splitText.length != 2) {
                    MessageManager.getInstance(event.getGuild()).sendLanguageChange(event.getTextChannel(), null);
                    break;
                }
                String lang = splitText[1].toUpperCase();

                if (MessageManager.checkLanguage(lang)) MessageManager.setLanguage(event.getGuild().getIdLong(), lang);
                else lang = null;

                MessageManager.getInstance(event.getGuild()).sendLanguageChange(event.getTextChannel(), lang);
                MessageManager.getInstance(event.getGuild()).sendHelp(event.getTextChannel());
                break;
            case "~rank":
                MessageManager.getInstance(event.getGuild())
                        .sendRank(event.getAuthor(),
                                event.getTextChannel(),
                                Objects.requireNonNull(DBManager.getRankingData(Settings.RANK_COUNT, event.getAuthor().getIdLong())));
                break;
            case "~start":
                User targetUser = null;
                if (event.getMessage().getMentionedUsers().size() > 0) targetUser = event.getMessage().getMentionedUsers().get(0);
                GameManager.createGame(event.getAuthor(), event.getTextChannel(), targetUser);
                break;
            case "~resign":
                GameManager.resignGame(event.getAuthor(), event.getTextChannel());
                break;
            case "~s":
                if ((splitText.length != 3)
                        || (!((splitText[1].length() == 1) && ((splitText[2].length() == 1)
                        || (splitText[2].length() == 2))))) {
                    MessageManager.getInstance(event.getGuild()).sendSyntaxError(event.getAuthor(), event.getTextChannel());
                    break;
                }

                Pos pos;
                try {
                    pos = new Pos(Pos.engToInt(splitText[1].toLowerCase().toCharArray()[0]), Integer.parseInt(splitText[2].toLowerCase()) - 1);
                } catch (Exception e) {
                    MessageManager.getInstance(event.getGuild()).sendSyntaxError(event.getAuthor(), event.getTextChannel());
                    break;
                }

                if (!Pos.checkSize(pos.getX(), pos.getY())) {
                    MessageManager.getInstance(event.getGuild()).sendSyntaxError(event.getAuthor(), event.getTextChannel());
                    break;
                }

                GameManager.putStone(pos, event.getAuthor(), event.getTextChannel());
                break;
        }
    }

    public static JDA getClient() {
        return BotManager.client;
    }

    public static TextChannel getOfficialChannel() {
        return Objects.requireNonNull(BotManager.getClient()
                .getGuildById(Settings.OFFICIAL_GUILD_ID))
                .getTextChannelById(Settings.RESULT_CHANNEL_ID);
    }

}
