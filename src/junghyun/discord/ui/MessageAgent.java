package junghyun.discord.ui;

import junghyun.ai.Pos;
import junghyun.discord.db.DBManager;
import junghyun.discord.db.Logger;
import junghyun.discord.ui.languages.LanguageKOR;
import junghyun.discord.game.ChatGame;
import junghyun.discord.unit.Settings;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

public class MessageAgent {

    private EmbedObject helpEmbed;
    private EmbedObject commandEmbed;

    private LanguageKOR languageContainer;

    public MessageAgent(LanguageKOR languageContainer) {
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
        helpBuilder.appendField(languageContainer.HELP_SUPPORT(), "[discord.gg/VkfMY6R](https://discord.gg/VkfMY6R)", true);

        this.helpEmbed = helpBuilder.build();

        //------------------------------------------------------------------------------------------------------------

        EmbedBuilder commandBuilder = new EmbedBuilder();

        commandBuilder.withAuthorName(languageContainer.HELP_CMD_INFO());
        commandBuilder.withColor(0,145,234);

        commandBuilder.appendField("~help", languageContainer.HELP_CMD_HELP(), false);
        commandBuilder.appendField("~lang", languageContainer.HELP_CMD_LANG(MessageManager.LANGUAGE_LIST), false);
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

    public void sendLanguageChange(IChannel channel, MessageManager.LANG lang) {
        if (lang == MessageManager.LANG.ERR) {
            channel.sendMessage(languageContainer.LANG_CHANGE_ERROR());
            sendLanguageChangeInfo(channel);
        } else channel.sendMessage(languageContainer.LANG_SUCCESS());
    }

    // Game Create/End Information

    // Error

    public void sendFailCreatedGame(IUser user, IChannel channel) {
        channel.sendMessage(languageContainer.GAME_CREATE_FAIL(user.getName()));
    }

    public void sendErrorGrammarSet(IUser user, IChannel channel) {
        channel.sendMessage(languageContainer.GAME_SYNTAX_FAIL(user.getName()));
    }

    public void sendAlreadyIn(ChatGame chatGame, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(languageContainer.GAME_ALREADY_IN(user.getName()));
    }

    // Create Game

    public void sendCreatedGame(ChatGame chatGame, boolean playerColor, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, user, channel);
        StringBuilder result = new StringBuilder();
        result.append(user.getName()).append(", the game has started. ");

        if (playerColor) result.append(user.getName()).append("is the first attack!");
        else result.append("I'm a first attack!");

        result.append(languageContainer.GAME_CMD_INFO());

        channel.sendMessage(result.toString());
    }

    // Progress Game

    public void sendNextTurn(ChatGame chatGame, Pos aiPos, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, aiPos, user, channel);
        chatGame.addMessage(channel.sendMessage(user.getName() + ", Please let us have the next move!"));
    }

    // End Game

    public void sendPlayerWin(ChatGame chatGame, Pos playerPos, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(user.getName() + ", you won by throwing it in `" + playerPos.getHumText() + "`. Congratulations! :grinning: ");
        this.deleteCanvasMessage(chatGame, channel);
    }

    public void sendPlayerLose(ChatGame chatGame, Pos aiPos, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(user.getName() + ", you were dead as I was throwing at `" + aiPos.getHumText() + "`! :sunglasses: ");
        this.deleteCanvasMessage(chatGame, channel);
    }

    public void sendResignPlayer(ChatGame chatGame, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(user.getName() + ", You surrendered. I won!:joy: ");
        this.deleteCanvasMessage(chatGame, channel);
    }

    // Error Game

    public void notFoundGame(IUser user, IChannel channel) {
        channel.sendMessage(user.getName()+ ", can not find the game you're playing. Start the game with `~ start`!");
    }

    public void sendFullCanvas(ChatGame chatGame, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(languageContainer.GAME_FULL());
        this.deleteCanvasMessage(chatGame, channel);
    }

    // Private Function

    private void deleteCanvasMessage(ChatGame chatGame, IChannel channel) {
        try {
            if (chatGame.getMessageList().size() > 0) channel.bulkDelete(chatGame.getMessageList());
        } catch (Exception e) {
            Logger.loggerWarning("Miss PERMISSION : " + channel.getGuild().getName());
        }
    }

    private void sendCanvasMessage(ChatGame chatGame, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, new Pos(-1, -1), user, channel);
    }

    private void sendCanvasMessage(ChatGame chatGame, Pos aiPos, IUser user, IChannel channel) {
        String statMsg;
        if (chatGame.getState() == ChatGame.STATE.INP) statMsg = languageContainer.BOARD_INP();
        else statMsg = languageContainer.BOARD_FINISH();

        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName(user.getName() + "#" + user.getDiscriminator() + ", " + statMsg);
        builder.withAuthorIcon(user.getAvatarURL());
        if (chatGame.getState() == ChatGame.STATE.INP) builder.withColor(0,200,83);
        else builder.withColor(213,0,0);

        builder.withDesc("withDesc");
        builder.withDescription(TextDrawer.getGraphics(chatGame.getGame(), aiPos));

        builder.appendField(languageContainer.BOARD_TURNS(), "#" + chatGame.getGame().getTurns() + languageContainer.BOARD_TURN(), true);
        builder.appendField(languageContainer.BOARD_LOCATION(), aiPos.getHumText(), true);

        if ((chatGame.getState() == ChatGame.STATE.INP) && (chatGame.getGame().getTurns() > 2)) chatGame.addMessage(channel.sendMessage(builder.build()));
        else channel.sendMessage(builder.build());
    }

}
