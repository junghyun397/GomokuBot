package junghyun.ui;

import junghyun.ai.Game;
import junghyun.db.DBManager;
import junghyun.unit.Pos;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public class Message {

    public static void sendHelp(IUser user, IChannel channel) {
        final String message = "여기 도움말이 있습니다! 하나씩 잘 읽어보세요. :)\n" +
                "\n**~help 도움말**, 도움말을 알려드립니다." +
                "\n**~start 게임 시작**: 게임을 시작합니다." +
                "\n**~rank 순위**: 자신의 순위와 다른사람들의 순위를 알려 드립니다." +
                "\n**~resign 항복**: 현재 진행하고 있는 게임을 포기합니다.";
        channel.sendMessage(message);
    }

    public static void sendRank(IUser user, IChannel channel, DBManager.UserDataSet[] rankData) {
        StringBuilder result = new StringBuilder("1위~10위 순위는 다음과 같습니다. :)\n");
        for (int i = 0; i < rankData.length; i++) result.append(i).append("위: ").append(rankData[i].getName())
                .append(" ").append(rankData[i].getWin()).append("승리").append("\n");

        channel.sendMessage(result.toString());
    }

    public static void sendCreatedGame(Game game, boolean playerColor, IUser user, IChannel channel) {
        StringBuilder result = new StringBuilder();
        result.append(TextDrawer.getGraphics(game)).append(user.getName()).append("님, 게임이 시작되었습니다, ");

        if (playerColor) result.append(user.getName()).append("님이 선공 이시네요!");
        else result.append("제가 선공입니다!");

        result.append(" **~s 알파벳 숫자** 형식으로 돌을 놓아주세요.");

        channel.sendMessage(result.toString());
    }

    public static void sendFailCreatedGame(IUser user, IChannel channel) {
        channel.sendMessage(user.getName() + "님, 게임 생성에 실패 했어요. 즐기고 계신 게임을 마무리 해주세요.");
    }

    public static void sendErrorGrammarSet(IUser user, IChannel channel) {
        channel.sendMessage(user.getName()+ "님, 그건 잘못된 명령어에요. **~s 알파벳 숫자** 형식으로 적어주세요!");
    }

    public static void sendAlreadyIn(IUser user, IChannel channel) {
        channel.sendMessage(user.getName()+ "님, 그곳에는 이미 돌이 놓여 있어요.");
    }

    public static void sendPlayerWin(Game game, Pos playerPos, IUser user, IChannel channel) {
        channel.sendMessage(TextDrawer.getGraphics(game, playerPos) + user.getName() + "님, "
                + playerPos.getHumX() + " " + playerPos.getHumY() + " 에 둠으로서 이기셨어요. 축하드립니다. XD");
    }

    public static void sendPlayerLose(Game game, Pos aiPos, IUser user, IChannel channel) {
        channel.sendMessage(TextDrawer.getGraphics(game, aiPos) + user.getName() + "님, "
                + " 제가 " + aiPos.getHumX() + " " + aiPos.getHumY() + " 에 둠으로서 지졌습니다. :/");
    }

    public static void sendNextTurn(Game game, Pos aiPos, IUser user, IChannel channel) {
        channel.sendMessage(TextDrawer.getGraphics(game, aiPos) + "#" + game.getTurns() + "턴: " +
                user.getName() + "님, " + " 전 " + aiPos.getHumX() + " " + aiPos.getHumY() + " 에 뒀어요!");
    }

    public static void notFoundGame(IUser user, IChannel channel) {
        channel.sendMessage(user.getName()+ "님, 하고계신 게임을 찾지 못했어요. ~start 로 게임을 시작 해주세요!");
    }

    public static void sendSurrenPlayer(Game game, IUser user, IChannel channel) {
        channel.sendMessage(TextDrawer.getGraphics(game) + user.getName() + "님, 항복하셨네요. 제가 이겼습니다!");
    }

    public static void sendFullCanvas(Game game, IUser user, IChannel channel) {
        channel.sendMessage(TextDrawer.getGraphics(game) + user.getName() + "님, "
                + "더이상 놓을 수 있는 자리가 없으므로 지셨습니다. :/");
    }

}
