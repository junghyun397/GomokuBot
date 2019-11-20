package junghyun.discord.ui;

import junghyun.ai.Pos;
import junghyun.discord.Settings;
import junghyun.discord.db.DBManager;
import junghyun.discord.db.Logger;
import junghyun.discord.game.ChatGame;
import junghyun.discord.ui.graphics.TextDrawer;
import junghyun.discord.ui.languages.LanguageInterface;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.awt.*;

@SuppressWarnings("unused")
public class MessageAgent {

    private MessageEmbed helpEmbed;
    private MessageEmbed commandEmbed;

    private LanguageInterface languageContainer;

    public MessageAgent(LanguageInterface languageContainer) {
        this.languageContainer = languageContainer;

        EmbedBuilder helpBuilder = new EmbedBuilder();

        helpBuilder.setAuthor(languageContainer.HELP_INFO());
        helpBuilder.setColor(new Color(0,145,234));
        helpBuilder.setDescription(languageContainer.HELP_DESCRIPTION());
        helpBuilder.setThumbnail("https://i.imgur.com/HAGBBT6.jpg");

        helpBuilder.addField(languageContainer.HELP_DEV(), "junghyun397#6725", true);
        helpBuilder.addField(languageContainer.HELP_GIT(), "[github.com/GomokuBot](https://github.com/junghyun397/GomokuBot)", true);
        helpBuilder.addField(languageContainer.HELP_VERSION(), Settings.VERSION, true);
        helpBuilder.addField(languageContainer.HELP_SUPPORT(), "[discord.gg/vq8pkfF](https://discord.gg/vq8pkfF)", true);

        this.helpEmbed = helpBuilder.build();

        //------------------------------------------------------------------------------------------------------------

        EmbedBuilder commandBuilder = new EmbedBuilder();

        commandBuilder.setAuthor(languageContainer.HELP_CMD_INFO());
        commandBuilder.setColor(new Color(0,145,234));

        commandBuilder.addField("~help", languageContainer.HELP_CMD_HELP(), false);
        commandBuilder.addField("~lang", languageContainer.HELP_CMD_LANG(MessageManager.LanguageList), false);
        commandBuilder.addField("~start", languageContainer.HELP_CMD_PVE(), false);
        commandBuilder.addField("~start @mention", languageContainer.HELP_CMD_PVP(), false);
        commandBuilder.addField("~resign", languageContainer.HELP_CMD_RESIGN(), false);

        this.commandEmbed = commandBuilder.build();
    }

    // Basic Information

    public void sendHelp(TextChannel channel) {
        channel.sendMessage(this.helpEmbed).complete();
        channel.sendMessage(this.commandEmbed).complete();
    }

    public void sendRank(User user, TextChannel channel, DBManager.UserDataSet[] rankData) {
        EmbedBuilder builder = new EmbedBuilder();

        builder.setAuthor(languageContainer.RANK_INFO());
        builder.setColor(new Color(0,145,234));
        builder.setDescription(languageContainer.RANK_DESCRIPTION());

        for (int i = 0; i < rankData.length; i++)
            builder.addField("#" + (i + 1) + ": " + rankData[i].getName(), languageContainer.RANK_WIN() + ": `" + rankData[i].getWin() +
                    "` " + languageContainer.RANK_LOSE() + ": `" + rankData[i].getLose() + "`", false);

        DBManager.UserDataSet userData = DBManager.getUserData(user.getIdLong());
        if (userData != null) builder.addField("#??: " + userData.getName(), languageContainer.RANK_WIN() + ": `" + userData.getWin() +
                "` " + languageContainer.RANK_LOSE() + ": `" + userData.getLose() + "`", false);

        channel.sendMessage(builder.build()).complete();
    }

    // Language Information

    public void sendLanguageInfo(TextChannel channel) {
        channel.sendMessage(MessageManager.langEmbed).complete();
    }

    public void sendLanguageChange(TextChannel channel, String lang) {
        if (lang == null) {
            channel.sendMessage(languageContainer.LANG_CHANGE_ERROR()).complete();
            sendLanguageInfo(channel);
        } else channel.sendMessage(languageContainer.LANG_SUCCESS()).complete();
    }

    // Game Create/End Information

    // Game Error

    public void sendCreatGameFail(User user, TextChannel channel) {
        channel.sendMessage(languageContainer.GAME_CREATE_FAIL(user.getName())).complete();
    }

    public void sendSyntaxError(User user, TextChannel channel) {
        channel.sendMessage(languageContainer.GAME_SYNTAX_FAIL(user.getName())).complete();
    }

    public void sendStoneAlreadyIn(ChatGame chatGame, TextChannel channel) {
        this.sendCanvasMessage(chatGame, channel);
        channel.sendMessage(languageContainer.GAME_ALREADY_IN(chatGame.getNameTag())).complete();
    }

    // Create Game

    public void sendCreateGameSuccess(ChatGame chatGame, String fPlayer, TextChannel channel) {
        this.sendCanvasMessage(chatGame, channel);

        String result = languageContainer.GAME_CREATE_INFO(chatGame.getNameTag(), chatGame.getOppPlayer().getNameTag(), fPlayer) +
                languageContainer.GAME_CMD_INFO();
        channel.sendMessage(result).complete();
    }

    // Progress Game

    public void sendNotPlayerTurn(String tPlayer, TextChannel channel) {
        channel.sendMessage(languageContainer.GAME_PVP_TURN(tPlayer)).complete();
    }

    public void sendNextTurn(ChatGame chatGame, Pos lastPos, String curPlayer, String prvPlayer, TextChannel channel) {
        this.sendCanvasMessage(chatGame, lastPos, channel);
        chatGame.addMessage(channel.sendMessage(languageContainer.GAME_NEXT_TURN(curPlayer, prvPlayer, lastPos.getHumText())).complete());
    }

    // End PvP Game

    public void sendPvPWin(ChatGame chatGame, Pos lastPos, String winPlayer, String losePlayer, TextChannel channel) {
        this.sendCanvasMessage(chatGame, channel);
        channel.sendMessage(languageContainer.GAME_PVP_WIN(winPlayer, losePlayer, lastPos.getHumText())).complete();
        this.deleteCanvasMessage(chatGame, channel);
    }

    public void sendPvPResign(ChatGame chatGame, String winPlayer, String losePlayer, TextChannel channel) {
        this.sendCanvasMessage(chatGame, channel);
        channel.sendMessage(languageContainer.GAME_PVP_RESIGN(winPlayer, losePlayer)).complete();
        this.deleteCanvasMessage(chatGame, channel);
    }

    public void sendPvPInfo(String winName, String loseName, TextChannel channel) {
        int winCount = 0, loseCount = 0;
        channel.sendMessage(languageContainer.GAME_PVP_INFO(winName, loseName, winCount, loseCount)).complete();
    }

    // End PvE Game

    public void sendPvEWin(ChatGame chatGame, Pos playerPos, TextChannel channel) {
        this.sendCanvasMessage(chatGame, channel);
        channel.sendMessage(languageContainer.GAME_PVE_WIN(playerPos.getHumText())).complete();
        this.deleteCanvasMessage(chatGame, channel);
    }

    public void sendPvELose(ChatGame chatGame, Pos aiPos, TextChannel channel) {
        this.sendCanvasMessage(chatGame, channel);
        channel.sendMessage(languageContainer.GAME_PVE_LOSE(aiPos.getHumText())).complete();
        this.deleteCanvasMessage(chatGame, channel);
    }

    public void sendPvEResign(ChatGame chatGame, TextChannel channel) {
        this.sendCanvasMessage(chatGame, channel);
        channel.sendMessage(languageContainer.GAME_PVE_RESIGN()).complete();
        this.deleteCanvasMessage(chatGame, channel);
    }

    public void sendPvEInfo(String playerName, TextChannel channel) {
        int winCount = 0, loseCount = 0, rank = 0;
        channel.sendMessage(languageContainer.GAME_PVE_INFO(playerName, winCount, loseCount, rank)).complete();
    }

    // Error Game

    public void sendNotFoundGame(User user, TextChannel channel) {
        channel.sendMessage(languageContainer.GAME_NOT_FOUND(user.getName())).complete();
    }

    public void sendFullCanvas(ChatGame chatGame, TextChannel channel) {
        this.sendCanvasMessage(chatGame, channel);
        channel.sendMessage(languageContainer.GAME_FULL()).complete();
        this.deleteCanvasMessage(chatGame, channel);
    }

    private void sendCanvasMessage(ChatGame chatGame, TextChannel channel) {
        this.sendCanvasMessage(chatGame, new Pos(-1, -1), channel);
    }

    private void sendCanvasMessage(ChatGame chatGame, Pos aiPos, TextChannel channel) {
        String statMsg;
        if (chatGame.getState() == ChatGame.STATE.INP) statMsg = languageContainer.BOARD_INP();
        else statMsg = languageContainer.BOARD_FINISH();

        EmbedBuilder builder = new EmbedBuilder();

        builder.setAuthor(chatGame.getNameTag() + "@" + chatGame.getOppPlayer().getNameTag() + ", " + statMsg,
                null, chatGame.getIconURL());
        if (chatGame.getState() == ChatGame.STATE.INP) builder.setColor(new Color(0,200,83));
        else builder.setColor(new Color(213,0,0));

        builder.setDescription(TextDrawer.getGraphics(chatGame.getGame(), aiPos));

        builder.addField(languageContainer.BOARD_TURNS(), " " + chatGame.getGame().getTurns() + languageContainer.BOARD_TURN(), true);
        builder.addField(languageContainer.BOARD_LOCATION(), aiPos.getHumText(), true);

        if ((chatGame.getState() == ChatGame.STATE.INP) && (chatGame.getGame().getTurns() > 2))
            chatGame.addMessage(channel.sendMessage(builder.build()).complete());
        else channel.sendMessage(builder.build()).complete();
    }

    // Official Post Function

    public static void postResultOfficialChannel(ChatGame chatGame, TextChannel channel) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(chatGame.getNameTag() + "@" + chatGame.getOppPlayer().getNameTag() + ", END",
                null, chatGame.getIconURL());
        builder.setColor(new Color(0,145,234));

        builder.setDescription(TextDrawer.getGraphics(chatGame.getGame(), new Pos(-1, -1)));

        String winner = chatGame.getNameTag();
        if (chatGame.getOppPlayer().getIsWin()) winner = chatGame.getOppPlayer().getNameTag();

        builder.addField("WINNER", winner, true);
        builder.addField("TURNS", String.valueOf(chatGame.getGame().getTurns()), true);

        channel.sendMessage(builder.build()).complete();
    }

    // Private Function

    private void deleteCanvasMessage(ChatGame chatGame, TextChannel channel) {
        try {
            if (chatGame.getMessageList().size() > 0) chatGame.getMessageList()
                    .forEach(msg -> channel.deleteMessageById(msg.getIdLong()).complete());
        } catch (Exception e) {
            Logger.loggerWarning("miss permission : " + channel.getName());
        }
    }

}
