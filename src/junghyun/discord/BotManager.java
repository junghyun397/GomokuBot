package junghyun.discord;

import junghyun.ai.Pos;
import junghyun.discord.db.DBManager;
import junghyun.discord.db.SqlManager;
import junghyun.discord.ui.MessageManager;
import junghyun.discord.unit.Settings;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.StatusType;

public class BotManager {

    private static IDiscordClient client;

    public static void startGomokuBot() {
        client = new ClientBuilder().setPresence(StatusType.ONLINE, ActivityType.WATCHING, "~help")
                .withToken(Settings.TOKEN).build();
        client.getDispatcher().registerListener(new EventListener());
        client.login();

        EventListener.onStartLoadGuilds();

        SqlManager.connectMysql();
        GameManager.bootGameManager();
        MessageManager.loadMessage();
    }

    public static void endGomokuBot() {
        BotManager.client.logout();
    }

    static void processCommand(MessageReceivedEvent event) {
        String[] splitText = event.getMessage().getContent().toLowerCase().split(" ");

        switch (splitText[0]) {
            case "~help":
                MessageManager.getInstance(event.getGuild()).sendHelp(event.getChannel());
                break;
            case "~lang":
                if (splitText.length != 2) {
                    MessageManager.getInstance(event.getGuild()).sendLanguageChange(event.getChannel(), MessageManager.LANG.ERR);
                    break;
                }
                MessageManager.LANG lang = MessageManager.getLangByString(splitText[1]);
                if (lang != MessageManager.LANG.ERR) MessageManager.setLanguage(event.getGuild().getLongID(), lang);

                MessageManager.getInstance(event.getGuild()).sendLanguageChange(event.getChannel(), lang);
                break;
            case "~rank":
                MessageManager.getInstance(event.getGuild()).sendRank(event.getAuthor(), event.getChannel(), DBManager.getRankingData(Settings.RANK_COUNT));
                break;
            case "~start":
                IUser targetUser = null;
                if (event.getMessage().getMentions().size() > 0) targetUser = event.getMessage().getMentions().get(0);
                GameManager.createGame(event.getAuthor(), event.getChannel(), targetUser);
                break;
            case "~resign":
                GameManager.resignGame(event.getAuthor(), event.getChannel());
                break;
            case "~s":
                if ((splitText.length != 3) || (!((splitText[1].length() == 1) && ((splitText[2].length() == 1) || (splitText[2].length() == 2))))) {
                    MessageManager.getInstance(event.getGuild()).sendErrorGrammarSet(event.getAuthor(), event.getChannel());
                    break;
                }

                Pos pos;
                try {
                    pos = new Pos(Pos.engToInt(splitText[1].toLowerCase().toCharArray()[0]), Integer.valueOf(splitText[2].toLowerCase()) - 1);
                } catch (Exception e) {
                    MessageManager.getInstance(event.getGuild()).sendErrorGrammarSet(event.getAuthor(), event.getChannel());
                    break;
                }
                if (!Pos.checkSize(pos.getX(), pos.getY())) {
                    MessageManager.getInstance(event.getGuild()).sendErrorGrammarSet(event.getAuthor(), event.getChannel());
                    break;
                }

                GameManager.putStone(event.getAuthor().getLongID(), pos, event.getAuthor(), event.getChannel());
                break;
        }
    }

    public static IDiscordClient getClient() {
        return BotManager.client;
    }

}
