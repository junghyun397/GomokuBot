package junghyun;

import junghyun.ai.engin.AIBase;
import junghyun.db.DBManager;
import junghyun.db.SqlManager;
import junghyun.ui.MessageManager;
import junghyun.unit.ChatGame;
import junghyun.unit.Pos;
import junghyun.unit.Settings;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;

class BotManager {

    private static IDiscordClient client;

    static void startGomokuBot() {
        client = new ClientBuilder().setPresence(StatusType.ONLINE, ActivityType.WATCHING, "~help")
                .withToken(Settings.TOKEN).build();
        client.getDispatcher().registerListener(new EventListener());
        client.login();

        SqlManager.connectMysql();
        GameManager.bootGameManager();
        MessageManager.loadMessage();
    }

    static void endGomokuBot() {
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
                AIBase.DIFF diff = AIBase.DIFF.MID;
                if (event.getMessage().getContent().equals("~start taiwan_no_1")) diff = AIBase.DIFF.EXT;
                GameManager.createGame(event.getAuthor().getLongID(), event.getAuthor(), event.getChannel(), diff, ChatGame.GAMETYPE.PVE);
                break;
            case "~resign":
                GameManager.resignGame(event.getAuthor().getLongID(), event.getAuthor(), event.getChannel());
                break;
            case "~s":
                if ((splitText.length != 3) || (!((splitText[1].length() == 1) && ((splitText[2].length() == 1) || (splitText[2].length() == 2))))) {
                    MessageManager.getInstance(event.getGuild()).sendErrorGrammarSet(event.getAuthor(), event.getChannel());
                    break;
                }

                Pos pos = new Pos(Pos.engToInt(splitText[1].toLowerCase().toCharArray()[0]), Integer.valueOf(splitText[2].toLowerCase())-1);
                if (!Pos.checkSize(pos.getX(), pos.getY())) {
                    MessageManager.getInstance(event.getGuild()).sendErrorGrammarSet(event.getAuthor(), event.getChannel());
                    break;
                }

                GameManager.putStone(event.getAuthor().getLongID(), pos, event.getAuthor(), event.getChannel());
                break;
        }
    }

    static IDiscordClient getClient() {
        return BotManager.client;
    }

}
