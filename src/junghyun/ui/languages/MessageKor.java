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

public class MessageKor extends MessageEng {

    private static EmbedObject helpEmbed;
    private static EmbedObject commandEmbed;

    public static void buildMessage() {
        EmbedBuilder helpBuilder = new EmbedBuilder();

        helpBuilder.withAuthorName("GomokuBot / 도움말");
        helpBuilder.withColor(0,145,234);
        helpBuilder.withDesc("withDesc");
        helpBuilder.withDescription("GomokuBot 은 Discord 에서 PvE 오목을 즐길 수 있게 해주는 오픈소스 Discord Bot 입니다. " +
                "수집된 기보 데이터는 강화학습 모델 훈련에 사용됩니다. :)");
        helpBuilder.withThumbnail("https://i.imgur.com/HAGBBT6.jpg");

        helpBuilder.appendField("개발자", "junghyun397#6725", true);
        helpBuilder.appendField("Git 저장소", "[github.com/GomokuBot](https://github.com/junghyun397/GomokuBot)", true);
        helpBuilder.appendField("판올림", Settings.VERSION, true);
        helpBuilder.appendField("지원 채널", "[discord.gg/VkfMY6R](https://discord.gg/VkfMY6R)", true);

        helpEmbed = helpBuilder.build();

        //------------------------------------------------------------------------------------------------------------

        EmbedBuilder commandBuilder = new EmbedBuilder();

        commandBuilder.withAuthorName("GomokuBot / 명령어");
        commandBuilder.withColor(0,145,234);

        commandBuilder.appendField("~help", "`~help` 도움말을 알려 드립니다.", false);
        commandBuilder.appendField("~lang", "`~lang` " + MessageManager.LANGUAGE_LIST +
                " 이 서버에서 사용되는 언어 설정을 바꿉니다. Ex) `~lang` `ENG`", false);
        commandBuilder.appendField("~start", "`~start` A.I 와의 게임을 시작합니다.", false);
        commandBuilder.appendField("~resign", "`~resign` 현재 진행하고 있는 게임을 포기합니다.", false);

        commandEmbed = commandBuilder.build();
    }

    // Basic Information

    public void sendHelp(IChannel channel) {
        channel.sendMessage(helpEmbed);
        channel.sendMessage(commandEmbed);
    }

    public void sendRank(IUser user, IChannel channel, DBManager.UserDataSet[] rankData) {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("GomokuBot / 순위");
        builder.withColor(0,145,234);
        builder.withDesc("withDesc");
        builder.withDescription("1위부터 10위까지의 순위 입니다. :D");

        for (int i = 0; i < rankData.length; i++)
            builder.appendField("#" + (i + 1) + ": " + rankData[i].getName(), "승리: `" + rankData[i].getWin() +
                    "` 패배: `" + rankData[i].getLose() + "`", false);

        DBManager.UserDataSet userData = DBManager.getUserData(user.getLongID());
        if (userData != null) builder.appendField("#??: " + userData.getName(), "승리: `" + userData.getWin() +
                "` 패배: `" + userData.getLose() + "`", false);

        channel.sendMessage(builder.build());
    }

    // Language Information

    public void sendLanguageChangeInfo(IChannel channel) {
        channel.sendMessage(MessageManager.langEmbed);
    }

    private void sendLanguageInfo(IChannel channel) {
        channel.sendMessage("`~lang` `바꿀 언어` 형식으로 적어 주세요. 현재 " + MessageManager.LANGUAGE_LIST + " 만을 지원 합니다.");
    }

    public void sendLanguageChange(IChannel channel, MessageManager.LANG lang) {
        if (lang == MessageManager.LANG.ERR) {
            channel.sendMessage("언어 지정에 오류가 있습니다!");
            sendLanguageInfo(channel);
        } else channel.sendMessage("언어 설정이 한국어:flag_kr:로 바뀌었습니다!");
    }

    // Game Create/End Information

    // Create Game

    public void sendCreatedGame(ChatGame chatGame, boolean playerColor, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, user, channel);
        StringBuilder result = new StringBuilder();
        if (chatGame.getDiff() == AIBase.DIFF.EAS) result.append(":turtle:쉬움 난이도로 진행합니다!:turtle:\n");
        if (chatGame.getDiff() == AIBase.DIFF.EXT) result.append(":fire:극한 난이도로 진행합니다!:fire:\n");
        result.append(user.getName()).append("님, 게임이 시작되었습니다. ");

        if (playerColor) result.append(user.getName()).append("님이 선공 이시네요!");
        else result.append("제가 선공입니다!");

        result.append(" `~s 알파벳 숫자` 형식으로 돌을 놓아주세요.");

        channel.sendMessage(result.toString());
    }

    public void sendFailCreatedGame(IUser user, IChannel channel) {
        channel.sendMessage(user.getName() + "님, 게임 생성에 실패 했어요. 즐기고 계신 게임을 마무리 해주세요. :thinking: ");
    }

    public void sendErrorGrammarSet(IUser user, IChannel channel) {
        channel.sendMessage(user.getName()+ "님, 그건 잘못된 명령어에요. `~s 알파벳 숫자` 형식으로 적어주세요. :thinking: ");
    }

    // End Game

    public void sendPlayerWin(ChatGame chatGame, Pos playerPos, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(user.getName() + "님, `" + playerPos.getHumText() + "` 에 둠으로서 이기셨어요. 축하드립니다! :grinning: ");
        this.deleteCanvasMessage(chatGame, channel);
    }

    public void sendPlayerLose(ChatGame chatGame, Pos aiPos, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(user.getName() + "님, " + " 제가 `" + aiPos.getHumText() + "` 에 둠으로서 지셨습니다. :sunglasses: ");
        this.deleteCanvasMessage(chatGame, channel);
    }

    public void sendResignPlayer(ChatGame chatGame, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(user.getName() + "님, 항복하셨네요. 제가 이겼습니다! :joy: ");
        this.deleteCanvasMessage(chatGame, channel);
    }

    public void sendFullCanvas(ChatGame chatGame, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(TextDrawer.getGraphics(chatGame.getGame()) + user.getName() + "님, 더이상 놓을 수 있는 자리가 없으므로 지셨습니다. :confused: ");
        this.deleteCanvasMessage(chatGame, channel);
    }

    // Progress Game

    public void sendAlreadyIn(ChatGame chatGame, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(user.getName()+ "님, 그곳에는 이미 돌이 놓여 있어요. :thinking: ");
    }

    public void sendNextTurn(ChatGame chatGame, Pos aiPos, IUser user, IChannel channel) {
        this.sendCanvasMessage(chatGame, aiPos, user, channel);
        chatGame.addMessage(channel.sendMessage(user.getName() + "님, 다음 수를 놓아 주세요!"));
    }

    // Error Game

    public void notFoundGame(IUser user, IChannel channel) {
        channel.sendMessage(user.getName()+ "님, 하고계신 게임을 찾지 못했어요. ~start 로 게임을 시작 해주세요!");
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
        if (chatGame.getState() == ChatGame.STATE.INP) statMsg = "진행중";
        else statMsg = "종료됨";

        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName(user.getName() + "#" + user.getDiscriminator() + ", " + statMsg);
        builder.withAuthorIcon(user.getAvatarURL());
        if (chatGame.getState() == ChatGame.STATE.INP) builder.withColor(0,200,83);
        else builder.withColor(213,0,0);

        builder.withDesc("withDesc");
        builder.withDescription(TextDrawer.getGraphics(chatGame.getGame(), aiPos));

        builder.appendField("턴 진행도", "#" + chatGame.getGame().getTurns() + "턴", true);
        builder.appendField("AI 착수 위치", aiPos.getHumText(), true);

        if ((chatGame.getState() == ChatGame.STATE.INP) && (chatGame.getGame().getTurns() > 2)) chatGame.addMessage(channel.sendMessage(builder.build()));
        else channel.sendMessage(builder.build());
    }

}
