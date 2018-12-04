package junghyun.ui.languages;

import junghyun.ai.engin.AIBase;
import junghyun.db.DBManager;
import junghyun.db.Logger;
import junghyun.ui.MessageManager;
import junghyun.ui.TextDrawer;
import junghyun.unit.ChatGame;
import junghyun.unit.Pos;
import junghyun.unit.Settings;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

public class MessageEng {

    private static EmbedObject helpEmbed;
    private static EmbedObject commandEmbed;

    public static void buildMessage() {
        EmbedBuilder helpBuilder = new EmbedBuilder();

        helpBuilder.withAuthorName("GomokuBot / Information");
        helpBuilder.withColor(0,145,234);
        helpBuilder.withDesc("withDesc");
        helpBuilder.withDescription("GomokuBot is an Open Source Discord Bot that allows you to enjoy PvE Gomoku at Discord. " +
                "The Collected data are used for training the Reinforcement Learning model. :)");
        helpBuilder.withThumbnail("https://i.imgur.com/HAGBBT6.jpg");

        helpBuilder.appendField("Developer", "junghyun397#6725", true);
        helpBuilder.appendField("Git Repository", "[github.com/GomokuBot](https://github.com/junghyun397/GomokuBot)", true);
        helpBuilder.appendField("Version", Settings.VERSION, true);
        helpBuilder.appendField("Support Channel", "[discord.gg/VkfMY6R](https://discord.gg/VkfMY6R)", true);

        helpEmbed = helpBuilder.build();

        //------------------------------------------------------------------------------------------------------------

        EmbedBuilder commandBuilder = new EmbedBuilder();

        commandBuilder.withAuthorName("GomokuBot / Command");
        commandBuilder.withColor(0,145,234);

        commandBuilder.appendField("~help", "`~help` Help is available.", false);
        commandBuilder.appendField("~lang", "`~lang` " + MessageManager.LANGUAGE_LIST +
                " Replace the language settings used by this Server. Ex) `~lang` `ENG`", false);
        commandBuilder.appendField("~start", "`~start` Start the game with A.I.", false);
        commandBuilder.appendField("~resign", "`~resign` Resign an ongoing Game", false);

        commandEmbed = commandBuilder.build();
    }

    // Basic Information

    public void sendHelp(IChannel channel) {
        channel.sendMessage(helpEmbed);
        channel.sendMessage(commandEmbed);
    }

    public void sendRank(IUser user, IChannel channel, DBManager.UserDataSet[] rankData) {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("GomokuBot / Ranking");
        builder.withColor(0,145,234);
        builder.withDesc("withDesc");
        builder.withDescription("It's ranked from 1st to 10th. :D");

        for (int i = 0; i < rankData.length; i++)
            builder.appendField("#" + (i + 1) + ": " + rankData[i].getName(), "Victory: `" + rankData[i].getWin() +
                    "` Defeat: `" + rankData[i].getLose() + "`", false);

        DBManager.UserDataSet userData = DBManager.getUserData(user.getLongID());
        if (userData != null) builder.appendField("#??: " + userData.getName(), "Victory: `" + userData.getWin() +
                "` Defeat: `" + userData.getLose() + "`", false);

        channel.sendMessage(builder.build());
    }

    // Language Information

    public void sendLanguageChangeInfo(IChannel channel) {
        channel.sendMessage(MessageManager.langEmbed);
    }

    private void sendLanguageInfo(IChannel channel) {
        channel.sendMessage("Please write it in the format `~lang` `New language` Currently, only "+ MessageManager.LANGUAGE_LIST + " is supported.");
    }

    public void sendLanguageChange(IChannel channel, MessageManager.LANG lang) {
        if (lang == MessageManager.LANG.ERR) {
            channel.sendMessage("There is an error in the language specification!");
            sendLanguageInfo(channel);
        } else channel.sendMessage("Language setting has been changed to English:flag_us:!");
    }

    // Game Create/End Information

    // Create Game

    public void sendCreatedGame(ChatGame chatGame, boolean playerColor, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, user, channel);
        StringBuilder result = new StringBuilder();
        if (chatGame.getDiff() == AIBase.DIFF.EAS) result.append(":turtle:Easy difficult!:turtle:\n");
        if (chatGame.getDiff() == AIBase.DIFF.EXT) result.append(":fire:Extreme difficulty!:fire:\n");
        result.append(user.getName()).append(", the game has started. ");

        if (playerColor) result.append(user.getName()).append("is the first attack!");
        else result.append("I'm a first attack!");

        result.append(" Place the stone in the form of `~s` `Alphabet` `Number`.");

        channel.sendMessage(result.toString());
    }

    public void sendFailCreatedGame(IUser user, IChannel channel) {
        channel.sendMessage(user.getName() + " failed to create the game. Please wrap up the game you're enjoying. :thinking: ");
    }

    public void sendErrorGrammarSet(IUser user, IChannel channel) {
        channel.sendMessage(user.getName()+ ", that is a wrong order. Please write it in the form of `~s` `Alphabet` `Number`. :thinking: ");
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

    public void sendFullCanvas(ChatGame chatGame, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(TextDrawer.getGraphics(chatGame.getGame()) + user.getName() + ", you lost because there are no more places to put. :confused: ");
        this.deleteCanvasMessage(chatGame, channel);
    }

    // Progress Game

    public void sendAlreadyIn(ChatGame chatGame, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(user.getName()+ ", there is already a stone there. :thinking: ");
    }

    public void sendNextTurn(ChatGame chatGame, Pos aiPos, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, aiPos, user, channel);
        chatGame.addMessage(channel.sendMessage(user.getName() + ", Please let us have the next move!"));
    }

    // Error Game

    public void notFoundGame(IUser user, IChannel channel) {
        channel.sendMessage(user.getName()+ ", can not find the game you're playing. Start the game with `~ start`!");
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
        if (chatGame.getState() == ChatGame.STATE.INP) statMsg = "Proceeding";
        else statMsg = "Finished";

        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName(user.getName() + "#" + user.getDiscriminator() + ", " + statMsg);
        builder.withAuthorIcon(user.getAvatarURL());
        if (chatGame.getState() == ChatGame.STATE.INP) builder.withColor(0,200,83);
        else builder.withColor(213,0,0);

        builder.withDesc("withDesc");
        builder.withDescription(TextDrawer.getGraphics(chatGame.getGame(), aiPos));

        builder.appendField("Turn progress", "#" + chatGame.getGame().getTurns() + "Turns", true);
        builder.appendField("AI launch location", aiPos.getHumText(), true);

        if ((chatGame.getState() == ChatGame.STATE.INP) && (chatGame.getGame().getTurns() > 2)) chatGame.addMessage(channel.sendMessage(builder.build()));
        else channel.sendMessage(builder.build());
    }

}
