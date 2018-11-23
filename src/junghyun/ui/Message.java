package junghyun.ui;

import junghyun.db.DBManager;
import junghyun.unit.ChatGame;
import junghyun.unit.Pos;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

public class Message {

    public static void sendHelp(IChannel channel) {
        final String message = "여기 도움말이 있습니다! 하나씩 잘 읽어보세요. :grinning: \n" +
                "\n`~help` 도움말: 도움말을 알려드립니다." +
                "\n`~start` 게임 시작: 게임을 시작합니다." +
                "\n`~rank` 순위: 순위를 알려 드립니다." +
                "\n`~resign` 항복: 현재 진행하고 있는 게임을 포기합니다.";
        channel.sendMessage(message);
    }

    public static void sendRank(IUser user, IChannel channel, DBManager.UserDataSet[] rankData) {
        StringBuilder result = new StringBuilder("1위~10위는 다음과 같습니다.\n");
        for (int i = 0; i < rankData.length; i++) result.append("#").append(i+1).append(": ").append(rankData[i].getName())
                .append(" `W/L ").append(rankData[i].getWin()).append("/").append(rankData[i].getLose()).append("`\n");

        DBManager.UserDataSet userData = DBManager.getUserData(user.getLongID());
        if (userData != null) channel.sendMessage(result.toString());
    }

    public static void sendCreatedGame(ChatGame chatGame, boolean playerColor, IUser user, IChannel channel) {
        Message.sendCanvasMessage(chatGame, user, channel);
        StringBuilder result = new StringBuilder();
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
        Message.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(user.getName()+ "님, 그곳에는 이미 돌이 놓여 있어요. :thinking: ");
    }

    public static void sendPlayerWin(ChatGame chatGame, Pos playerPos, IUser user, IChannel channel) {
        if (chatGame.getMessageList().size() > 0) channel.bulkDelete(chatGame.getMessageList());
        Message.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(user.getName() + "님, `" + playerPos.getHumText() + "` 에 둠으로서 이기셨어요. 축하드립니다! :grinning: ");
    }

    public static void sendPlayerLose(ChatGame chatGame, Pos aiPos, IUser user, IChannel channel) {
        if (chatGame.getMessageList().size() > 0) channel.bulkDelete(chatGame.getMessageList());
        Message.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(user.getName() + "님, " + " 제가 `" + aiPos.getHumText() + "` 에 둠으로서 지졌습니다. :sunglasses: ");
    }

    public static void sendNextTurn(ChatGame chatGame, Pos aiPos, IUser user, IChannel channel) {
        Message.sendCanvasMessage(chatGame, aiPos, user, channel);
        chatGame.addMessage(channel.sendMessage(user.getName() + "님, 다음 수를 놓아 주세요!"));
    }

    public static void notFoundGame(IUser user, IChannel channel) {
        channel.sendMessage(user.getName()+ "님, 하고계신 게임을 찾지 못했어요. ~start 로 게임을 시작 해주세요!");
    }

    public static void sendResignPlayer(ChatGame chatGame, IUser user, IChannel channel) {
        if (chatGame.getMessageList().size() > 0) channel.bulkDelete(chatGame.getMessageList());
        Message.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(user.getName() + "님, 항복하셨네요. 제가 이겼습니다! :joy: ");
    }

    public static void sendFullCanvas(ChatGame chatGame, IUser user, IChannel channel) {
        if (chatGame.getMessageList().size() > 0) channel.bulkDelete(chatGame.getMessageList());
        Message.sendCanvasMessage(chatGame, user, channel);
        channel.sendMessage(TextDrawer.getGraphics(chatGame.getGame()) + user.getName() + "님, 더이상 놓을 수 있는 자리가 없으므로 지셨습니다. :confused: ");
    }

    private static void sendCanvasMessage(ChatGame chatGame, IUser user, IChannel channel) {
        Message.sendCanvasMessage(chatGame, new Pos(-1, -1), user, channel);
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
