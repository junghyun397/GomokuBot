package junghyun.ui.languages;

import junghyun.ai.engin.AIBase;
import junghyun.db.DBManager;
import junghyun.db.Logger;
import junghyun.ui.MessageBase;
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
    private static EmbedObject langEmbed;

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

        helpBuilder.appendField("명령어 안내", "**~help** 도움말을 알려 드립니다.\n\n" +
                "**~rank** 전체 TOP 10 순위를 알려 드립니다.\n\n" +
                "**~lang** `ENG`/`KOR` 언어를 변경합니다.\n\n" +
                "**~start** 게임을 시작합니다.\n\n" +
                "**~resign** 현재 진행하고 있는 게임을 포기합니다.", false);

        MessageEng.helpEmbed = helpBuilder.build();

        //------------------------------------------------------------------------------------------------------------

        EmbedBuilder langBuilder = new EmbedBuilder();

        langBuilder.withAuthorName("언어 안내");
        langBuilder.withColor(0,145,234);
        langBuilder.withDesc("withDesc");
        langBuilder.withDescription("이 서버에 알맞는 언어를 선택 해주세요!");

        langBuilder.appendField("영어:flag_us:", "`~lang ENG` 명령어를 사용 해주세요.", false);
        langBuilder.appendField("한국어:flag_kr:", "`~lang KOR` 명령어를 사용 해주세요.", false);

        MessageEng.langEmbed = langBuilder.build();
    }

    public static void sendHelp(IChannel channel) {
        channel.sendMessage(MessageEng.helpEmbed);
    }

    public static void sendLanguageChangeInfo(IChannel channel) {
        channel.sendMessage(MessageEng.langEmbed);
    }

    public static void sendRank(IUser user, IChannel channel, DBManager.UserDataSet[] rankData) {
        EmbedBuilder builder = new EmbedBuilder();

        builder.withAuthorName("GomokuBot / 순위");
        builder.withColor(0,145,234);
        builder.withDesc("withDesc");
        builder.withDescription("1위부터 10위까지의 순위 입니다. :D");

        for (int i = 0; i < rankData.length; i++)
            builder.appendField("#" + (i + 1) + ": " + rankData[i].getName(), "승리: `" + rankData[i].getWin() +
                    "` 패배: `" + rankData[i].getLose() + "`", true);

        DBManager.UserDataSet userData = DBManager.getUserData(user.getLongID());
        if (userData != null) builder.appendField("#??: " + userData.getName(), "승리: `" + userData.getWin() +
                "` 패배: `" + userData.getLose() + "`", true);

        channel.sendMessage(builder.build());
    }

    private static void sendLanguageInfo(IChannel channel) {
        channel.sendMessage("~lang `바꿀 언어` 형식으로 적어 주세요. 현재 `ENG`, `KOR` 만을 지원 합니다.");
    }

    public static void sendLanguageChange(IChannel channel, MessageBase.LANG lang) {
        if (lang == MessageBase.LANG.ERR) {
            channel.sendMessage("언어 지정에 오류가 있습니다!");
            sendLanguageInfo(channel);
        } else channel.sendMessage("언어 설정이 영어:flag_us:로 바뀌었습니다!");
    }

    public static void sendCreatedGame(ChatGame chatGame, boolean playerColor, IUser user, IChannel channel) {
        MessageEng.sendCanvasMessage(chatGame, user, channel);
        StringBuilder result = new StringBuilder();
        if (chatGame.getDiff() == AIBase.DIFF.EAS) result.append(":turtle:쉬움 난이도로 진행합니다!:turtle:\n");
        if (chatGame.getDiff() == AIBase.DIFF.EXT) result.append(":fire:극한 난이도로 진행합니다!:fire:\n");
        result.append(user.getName()).append("님, 게임이 시작되었습니다. ");

        if (playerColor) result.append(user.getName()).append("님이 선공 이시네요!");
        else result.append("제가 선공입니다!");

        result.append(" `~s 알파벳 숫자` 형식으로 돌을 놓아주세요.");

        channel.sendMessage(result.toString());
    }

    public static void sendFailCreatedGame(IUser user, IChannel channel) {
        channel.sendMessage(user.getName() + "님, 게임 생성에 실패 했어요. 즐기고 계신 게임을 마무리 해주세요. :thinking: ");
    }

    public static void sendErrorGrammarSet(IUser user, IChannel channel) {
        channel.sendMessage(user.getName()+ "님, 그건 잘못된 명령어에요. `~s 알파벳 숫자` 형식으로 적어주세요. :thinking: ");
    }

    public static void sendAlreadyIn(ChatGame chatGame, IUser user, IChannel channel) {
        MessageEng.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(user.getName()+ "님, 그곳에는 이미 돌이 놓여 있어요. :thinking: ");
    }

    public static void sendPlayerWin(ChatGame chatGame, Pos playerPos, IUser user, IChannel channel) {
        MessageEng.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(user.getName() + "님, `" + playerPos.getHumText() + "` 에 둠으로서 이기셨어요. 축하드립니다! :grinning: ");
        MessageEng.deleteCanvasMessage(chatGame, channel);
    }

    public static void sendPlayerLose(ChatGame chatGame, Pos aiPos, IUser user, IChannel channel) {
        MessageEng.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(user.getName() + "님, " + " 제가 `" + aiPos.getHumText() + "` 에 둠으로서 지졌습니다. :sunglasses: ");
        MessageEng.deleteCanvasMessage(chatGame, channel);
    }

    public static void sendNextTurn(ChatGame chatGame, Pos aiPos, IUser user, IChannel channel) {
        MessageEng.sendCanvasMessage(chatGame, aiPos, user, channel);
        chatGame.addMessage(channel.sendMessage(user.getName() + "님, 다음 수를 놓아 주세요!"));
    }

    public static void notFoundGame(IUser user, IChannel channel) {
        channel.sendMessage(user.getName()+ "님, 하고계신 게임을 찾지 못했어요. ~start 로 게임을 시작 해주세요!");
    }

    public static void sendResignPlayer(ChatGame chatGame, IUser user, IChannel channel) {
        MessageEng.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(user.getName() + "님, 항복하셨네요. 제가 이겼습니다! :joy: ");
        MessageEng.deleteCanvasMessage(chatGame, channel);
    }

    public static void sendFullCanvas(ChatGame chatGame, IUser user, IChannel channel) {
        MessageEng.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(TextDrawer.getGraphics(chatGame.getGame()) + user.getName() + "님, 더이상 놓을 수 있는 자리가 없으므로 지셨습니다. :confused: ");
        MessageEng.deleteCanvasMessage(chatGame, channel);
    }

    private static void deleteCanvasMessage(ChatGame chatGame, IChannel channel) {
        try {
            if (chatGame.getMessageList().size() > 0) channel.bulkDelete(chatGame.getMessageList());
        } catch (Exception e) {
            Logger.loggerWarning("Miss PERMISSION : " + channel.getGuild().getName());
        }
    }

    private static void sendCanvasMessage(ChatGame chatGame, IUser user, IChannel channel) {
        MessageEng.sendCanvasMessage(chatGame, new Pos(-1, -1), user, channel);
    }

    private static void sendCanvasMessage(ChatGame chatGame, Pos aiPos, IUser user, IChannel channel) {
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
