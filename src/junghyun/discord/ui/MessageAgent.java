package junghyun.discord.ui;

import junghyun.ai.Pos;
import junghyun.discord.Settings;
import junghyun.discord.db.DBManager;
import junghyun.discord.db.Logger;
import junghyun.discord.game.ChatGame;
import junghyun.discord.ui.graphics.TextDrawer;
import junghyun.discord.ui.languages.LanguageInterface;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

public class MessageAgent {

    private EmbedObject helpEmbed;
    private EmbedObject commandEmbed;

    private LanguageInterface languageContainer;

    public MessageAgent(LanguageInterface languageContainer) {
        this.languageContainer = languageContainer;

        EmbedBuilder helpBuilder = new EmbedBuilder();

        helpBuilder.withAuthorName(languageContainer.HELP_INFO());
        helpBuilder.withColor(0,145,234);
        helpBuilder.withDesc("withDesc");
        helpBuilder.withDescription(languageContainer.HELP_DESCRIPTION());
        helpBuilder.withThumbnail("https://i.imgur.com/HAGBBT6.jpg");

        helpBuilder.appendField(languageContainer.HELP_DEV(), "junghyun397#6725", true);
        helpBuilder.appendField(languageContainer.HELP_GIT(), "[github.com/GomokuBot](https://github.com/junghyun397/GomokuBot)", true);
        helpBuilder.appendField(languageContainer.HELP_VERSION(), Settings.VERSION, true);
        helpBuilder.appendField(languageContainer.HELP_SUPPORT(), "[discord.gg/vq8pkfF](https://discord.gg/vq8pkfF)", true);

        this.helpEmbed = helpBuilder.build();

        //------------------------------------------------------------------------------------------------------------

        EmbedBuilder commandBuilder = new EmbedBuilder();

        commandBuilder.withAuthorName(languageContainer.HELP_CMD_INFO());
        commandBuilder.withColor(0,145,234);

        commandBuilder.appendField("~help", languageContainer.HELP_CMD_HELP(), false);
        commandBuilder.appendField("~lang", languageContainer.HELP_CMD_LANG(MessageManager.LanguageList), false);
        commandBuilder.appendField("~start", languageContainer.HELP_CMD_PVE(), false);
        commandBuilder.appendField("~start @mention", languageContainer.HELP_CMD_PVP(), false);
        commandBuilder.appendField("~resign", languageContainer.HELP_CMD_RESIGN(), false);

        this.commandEmbed = commandBuilder.build();
    }

    // Basic Information

    public void sendHelp(IChannel channel) {
        channel.sendMessage(this.helpEmbed);
        channel.sendMessage(this.commandEmbed);
    }

    public void sendRank(IUser user, IChannel channel, DBManager.UserDataSet[] rankData) {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName(languageContainer.RANK_INFO());
        builder.withColor(0,145,234);
        builder.withDesc("withDesc");
        builder.withDescription(languageContainer.RANK_DESCRIPTION());

        for (int i = 0; i < rankData.length; i++)
            builder.appendField("#" + (i + 1) + ": " + rankData[i].getName(), languageContainer.RANK_WIN() + ": `" + rankData[i].getWin() +
                    "` " + languageContainer.RANK_LOSE() + ": `" + rankData[i].getLose() + "`", false);

        DBManager.UserDataSet userData = DBManager.getUserData(user.getLongID());
        if (userData != null) builder.appendField("#??: " + userData.getName(), languageContainer.RANK_WIN() + ": `" + userData.getWin() +
                "` " + languageContainer.RANK_LOSE() + ": `" + userData.getLose() + "`", false);

        channel.sendMessage(builder.build());
    }

    // Language Information

    public void sendLanguageChangeInfo(IChannel channel) {
        channel.sendMessage(MessageManager.langEmbed);
    }

    public void sendLanguageChange(IChannel channel, String lang) {
        if (lang == null) {
            channel.sendMessage(languageContainer.LANG_CHANGE_ERROR());
            sendLanguageChangeInfo(channel);
        } else channel.sendMessage(languageContainer.LANG_SUCCESS());
    }

    // Game Create/End Information

    // Game Error

    public void sendFailCreatedGame(IUser user, IChannel channel) {
        channel.sendMessage(languageContainer.GAME_CREATE_FAIL(user.getName()));
    }

    public void sendErrorGrammarSet(IUser user, IChannel channel) {
        channel.sendMessage(languageContainer.GAME_SYNTAX_FAIL(user.getName()));
    }

    public void sendAlreadyIn(ChatGame chatGame, IChannel channel) {
        this.sendCanvasMessage(chatGame, channel);
        channel.sendMessage(languageContainer.GAME_ALREADY_IN(chatGame.getNameTag()));
    }

    // Create Game

    public void sendCreatedGame(ChatGame chatGame, String fPlayer, IChannel channel) {
        this.sendCanvasMessage(chatGame, channel);

        String result = languageContainer.GAME_CREATE_INFO(chatGame.getNameTag(), chatGame.getOppPlayer().getNameTag(), fPlayer) +
                languageContainer.GAME_CMD_INFO();
        channel.sendMessage(result);
    }

    // Progress Game

    public void sendNotPlayerTurn(String tPlayer, IChannel channel) {
        channel.sendMessage(languageContainer.GAME_PVP_TURN(tPlayer));
    }

    public void sendNextTurn(ChatGame chatGame, Pos lastPos, String curPlayer, String prvPlayer, IChannel channel) {
        this.sendCanvasMessage(chatGame, lastPos, channel);
        chatGame.addMessage(channel.sendMessage(languageContainer.GAME_NEXT_TURN(curPlayer, prvPlayer, lastPos.getHumText())));
    }

    // End PvP Game

    public void sendPvPWin(ChatGame chatGame, Pos lastPos, String winPlayer, String losePlayer, IChannel channel) {
        this.sendCanvasMessage(chatGame, channel);
        channel.sendMessage(languageContainer.GAME_PVP_WIN(winPlayer, losePlayer, lastPos.getHumText()));
        this.deleteCanvasMessage(chatGame, channel);
    }

    public void sendPvPResign(ChatGame chatGame, String winPlayer, String losePlayer, IChannel channel) {
        this.sendCanvasMessage(chatGame, channel);
        channel.sendMessage(languageContainer.GAME_PVP_RESIGN(winPlayer, losePlayer));
        this.deleteCanvasMessage(chatGame, channel);
    }

    public void sendPvPInfo(String winName, String loseName, int winCount, int loseCount, IChannel channel) {
        channel.sendMessage(languageContainer.GAME_PVP_INFO(winName, loseName, winCount, loseCount));
    }

    // End PvE Game

    public void sendPvEWin(ChatGame chatGame, Pos playerPos, IChannel channel) {
        this.sendCanvasMessage(chatGame, channel);
        channel.sendMessage(languageContainer.GAME_PVE_WIN(playerPos.getHumText()));
        this.deleteCanvasMessage(chatGame, channel);
    }

    public void sendPvELose(ChatGame chatGame, Pos aiPos, IChannel channel) {
        this.sendCanvasMessage(chatGame, channel);
        channel.sendMessage(languageContainer.GAME_PVE_LOSE(aiPos.getHumText()));
        this.deleteCanvasMessage(chatGame, channel);
    }

    public void sendPvEResign(ChatGame chatGame, IChannel channel) {
        this.sendCanvasMessage(chatGame, channel);
        channel.sendMessage(languageContainer.GAME_PVE_RESIGN());
        this.deleteCanvasMessage(chatGame, channel);
    }

    public void sendPvEInfo(String playerName, int winCount, int loseCount, int rank, IChannel channel) {
        channel.sendMessage(languageContainer.GAME_PVE_INFO(playerName, winCount, loseCount, rank));
    }

    // Error Game

    public void notFoundGame(IUser user, IChannel channel) {
        channel.sendMessage(languageContainer.GAME_NOT_FOUND(user.getName()));
    }

    public void sendFullCanvas(ChatGame chatGame, IChannel channel) {
        this.sendCanvasMessage(chatGame, channel);
        channel.sendMessage(languageContainer.GAME_FULL());
        this.deleteCanvasMessage(chatGame, channel);
    }

    private void sendCanvasMessage(ChatGame chatGame, IChannel channel) {
        this.sendCanvasMessage(chatGame, new Pos(-1, -1), channel);
    }

    private void sendCanvasMessage(ChatGame chatGame, Pos aiPos, IChannel channel) {
        String statMsg;
        if (chatGame.getState() == ChatGame.STATE.INP) statMsg = languageContainer.BOARD_INP();
        else statMsg = languageContainer.BOARD_FINISH();

        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName(chatGame.getNameTag() + "#" + chatGame.getOppPlayer().getNameTag() + ", " + statMsg);
        builder.withAuthorIcon(chatGame.getIconURL());
        if (chatGame.getState() == ChatGame.STATE.INP) builder.withColor(0,200,83);
        else builder.withColor(213,0,0);

        builder.withDesc("withDesc");
        builder.withDescription(TextDrawer.getGraphics(chatGame.getGame(), aiPos));

        builder.appendField(languageContainer.BOARD_TURNS(), " VS." + chatGame.getGame().getTurns() + languageContainer.BOARD_TURN(), true);
        builder.appendField(languageContainer.BOARD_LOCATION(), aiPos.getHumText(), true);

        if ((chatGame.getState() == ChatGame.STATE.INP) && (chatGame.getGame().getTurns() > 2)) chatGame.addMessage(channel.sendMessage(builder.build()));
        else channel.sendMessage(builder.build());
    }

    // Private Function

    private void deleteCanvasMessage(ChatGame chatGame, IChannel channel) {
        try {
            if (chatGame.getMessageList().size() > 0) channel.bulkDelete(chatGame.getMessageList());
        } catch (Exception e) {
            Logger.loggerWarning("Miss PERMISSION : " + channel.getGuild().getName());
        }
    }

}
