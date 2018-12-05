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

public class MessageCHN extends MessageENG {

    private static EmbedObject helpEmbed;
    private static EmbedObject commandEmbed;

    public static void buildMessage() {
        EmbedBuilder helpBuilder = new EmbedBuilder();

        helpBuilder.withAuthorName("GomokuBot / 帮助");
        helpBuilder.withColor(0,145,234);
        helpBuilder.withDesc("withDesc");
        helpBuilder.withDescription("GomokuBot 是一个可以让您在 Discord上享受 PvE 五子棋的开源 Discord Bot。本机器人所收集的棋谱数据将会被用于学习模型的训练。:)");
        helpBuilder.withThumbnail("https://i.imgur.com/HAGBBT6.jpg");

        helpBuilder.appendField("开发人员", "junghyun397#6725", true);
        helpBuilder.appendField("Git 存储区", "[github.com/GomokuBot](https://github.com/junghyun397/GomokuBot)", true);
        helpBuilder.appendField("版本", Settings.VERSION, true);
        helpBuilder.appendField("可使用频道", "[discord.gg/VkfMY6R](https://discord.gg/VkfMY6R)", true);

        helpEmbed = helpBuilder.build();

        //------------------------------------------------------------------------------------------------------------

        EmbedBuilder commandBuilder = new EmbedBuilder();

        commandBuilder.withAuthorName("GomokuBot / 命令");
        commandBuilder.withColor(0,145,234);

        commandBuilder.appendField("~help", "`~help` 提供帮助", false);
        commandBuilder.appendField("~lang", "`~lang` " + MessageManager.LANGUAGE_LIST + " 更改语言 Ex) `~lang` `ENG`", false);
        commandBuilder.appendField("~start", "`~start` 开始游戏", false);
        commandBuilder.appendField("~resign", "`~resign` 抛弃游戏", false);

        commandEmbed = commandBuilder.build();
    }

    // Basic Information

    public void sendHelp(IChannel channel) {
        channel.sendMessage(helpEmbed);
        channel.sendMessage(commandEmbed);
    }

    public void sendRank(IUser user, IChannel channel, DBManager.UserDataSet[] rankData) {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("GomokuBot / 顺位");
        builder.withColor(0,145,234);
        builder.withDesc("withDesc");
        builder.withDescription("从第一到第十的顺位 :D");

        for (int i = 0; i < rankData.length; i++)
            builder.appendField("#" + (i + 1) + ": " + rankData[i].getName(), "胜: `" + rankData[i].getWin() +
                    "` 败: `" + rankData[i].getLose() + "`", false);

        DBManager.UserDataSet userData = DBManager.getUserData(user.getLongID());
        if (userData != null) builder.appendField("#??: " + userData.getName(), "胜: `" + userData.getWin() +
                "` 败: `" + userData.getLose() + "`", false);

        channel.sendMessage(builder.build());
    }

    // Language Information

    public void sendLanguageChange(IChannel channel, MessageManager.LANG lang) {
        if (lang == MessageManager.LANG.ERR) {
            channel.sendMessage("语言指定错误！");
            sendLanguageChangeInfo(channel);
        } else channel.sendMessage("已改为汉语:flag_cn:!");
    }

    // Game Create/End Information

    // Create Game

    public void sendCreatedGame(ChatGame chatGame, boolean playerColor, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, user, channel);
        StringBuilder result = new StringBuilder();
        if (chatGame.getDiff() == AIBase.DIFF.EAS) result.append(":turtle:以简单模式进行！:turtle:\n");
        if (chatGame.getDiff() == AIBase.DIFF.EXT) result.append(":fire:以超难模式进行！:fire:\n");
        result.append(user.getName()).append("，游戏已开始！");

        if (playerColor) result.append(user.getName()).append("Name，请先下棋！");
        else result.append("我先下棋！");

        result.append("请以`~s` 字母 数字`形式下棋！");

        channel.sendMessage(result.toString());
    }

    public void sendFailCreatedGame(IUser user, IChannel channel) {
        channel.sendMessage(user.getName() + "，无法生成新游戏，请结束现在游戏。 :thinking: ");
    }

    public void sendErrorGrammarSet(IUser user, IChannel channel) {
        channel.sendMessage(user.getName()+ "Name，无法识别。请以`~s 字母 数字` 下棋！ :thinking: ");
    }

    // End Game

    public void sendPlayerWin(ChatGame chatGame, Pos playerPos, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(user.getName() + "，您因为下在 `" + playerPos.getHumText() + "` 所以您赢了，祝贺！ :grinning: ");
        this.deleteCanvasMessage(chatGame, channel);
    }

    public void sendPlayerLose(ChatGame chatGame, Pos aiPos, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(user.getName() + "，因为我下在 `" + aiPos.getHumText() + "` 所以您输了。 :sunglasses: ");
        this.deleteCanvasMessage(chatGame, channel);
    }

    public void sendResignPlayer(ChatGame chatGame, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(user.getName() + "，因为您投降，所以我赢了！ :joy: ");
        this.deleteCanvasMessage(chatGame, channel);
    }

    public void sendFullCanvas(ChatGame chatGame, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(TextDrawer.getGraphics(chatGame.getGame()) + user.getName() + "，因为您没有地方可下棋，所以您输了！ :confused: ");
        this.deleteCanvasMessage(chatGame, channel);
    }

    // Progress Game

    public void sendAlreadyIn(ChatGame chatGame, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(user.getName()+ "，该处已有棋子！ :thinking: ");
    }

    public void sendNextTurn(ChatGame chatGame, Pos aiPos, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, aiPos, user, channel);
        chatGame.addMessage(channel.sendMessage(user.getName() + "，请下下一步棋！"));
    }

    // Error Game

    public void notFoundGame(IUser user, IChannel channel) {
        channel.sendMessage(user.getName()+ "，找不到现在正在进行的游戏， 请输入 ~start 来开始新游戏！");
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
        if (chatGame.getState() == ChatGame.STATE.INP) statMsg = "正在进行";
        else statMsg = "已完成";

        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName(user.getName() + "#" + user.getDiscriminator() + ", " + statMsg);
        builder.withAuthorIcon(user.getAvatarURL());
        if (chatGame.getState() == ChatGame.STATE.INP) builder.withColor(0,200,83);
        else builder.withColor(213,0,0);

        builder.withDesc("withDesc");
        builder.withDescription(TextDrawer.getGraphics(chatGame.getGame(), aiPos));

        builder.appendField("回合进行度", "#" + chatGame.getGame().getTurns() + "回合", true);
        builder.appendField("AI下棋位置", aiPos.getHumText(), true);

        if ((chatGame.getState() == ChatGame.STATE.INP) && (chatGame.getGame().getTurns() > 2)) chatGame.addMessage(channel.sendMessage(builder.build()));
        else channel.sendMessage(builder.build());
    }

}
