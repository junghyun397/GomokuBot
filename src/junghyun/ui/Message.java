package junghyun.ui;

import junghyun.ai.Game;
import junghyun.unit.Pos;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public class Message {

    public static void sendHelp(IUser user, IChannel channel) {
        channel.sendMessage("도움말!");
    }

    public static void sendRank(IUser user, IChannel channel) {
        channel.sendMessage("순위!");
    }

    public static void sendCreatedGame(IUser user, IChannel channel) {
        channel.sendMessage("생성됨 게임!");
    }

    public static void sendFailCreatedGame(IUser user, IChannel channel) {
        channel.sendMessage("생성 실패 게임!");
    }

    public static void sendPlayerWin(Game game, Pos playerPos, IUser user, IChannel channel) {
        channel.sendMessage("플레이어 승리!");
    }

    public static void sendPlayerLose(Game game, Pos aiPos, IUser user, IChannel channel) {
        channel.sendMessage("플레이어 패배!");
    }

    public static void sendNextTurn(Game game, Pos aiPos, IUser user, IChannel channel) {
        channel.sendMessage(TextDrawer.getGraphics(game, aiPos) + "\n");
    }

    public static void notFoundGame(IUser iUser, IChannel channel) {
        channel.sendMessage("게임을 찾을 수 없음!");
    }

    public static void sendSurrenPlayer(Game game, IUser user, IChannel channel) {
        channel.sendMessage("플레이어 패배!");
    }

}
