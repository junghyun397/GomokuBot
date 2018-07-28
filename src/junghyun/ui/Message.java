package junghyun.ui;

import junghyun.ai.Game;
import junghyun.unit.Pos;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public class Message {

    public static void sendHelp(IUser user, IChannel channel) {
        channel.sendMessage("여기 도움말이 있습니다! 하나씩 잘 읽어보세요. :)\n\n**!help 도움말**, 도움말을 알려드립니다.\n**!start 게임 시작**, 게임을 시작합니다.\n**!rank 순위**, 자신의 순위와 다른사람들의 순위를 알려 드립니다.");
    }

    public static void sendRank(IUser user, IChannel channel) {
        channel.sendMessage("순위!");
    }

    public static void sendCreatedGame(Game game, boolean playerColor, IUser user, IChannel channel) {
        String turnText;
        if (playerColor) turnText = user.getName() + "님이 선공 이시네요!";
        else turnText = "제가 선공입니다!";
        String result = TextDrawer.getGraphics(game) +
                user.getName() + "님, 게임이 시작되었습니다, " + turnText + " !s 알파벳 숫자 형식으로 돌을 놓아주세요.";
        channel.sendMessage(result);
    }

    public static void sendFailCreatedGame(IUser user, IChannel channel) {
        channel.sendMessage(user.getName()+ "님, 게임 생성에 실패 했어요. 즐기고 계신 게임을 마무리 해주세요.");
    }

    public static void sendErrorGrammarSet(IUser user, IChannel channel) {
        channel.sendMessage(user.getName()+ "님, 그건 잘못된 명령어에요. !s 알파벳 숫자 형식으로 적어주세요!");
    }

    public static void sendPlayerWin(Game game, Pos playerPos, IUser user, IChannel channel) {
        String result = TextDrawer.getGraphics(game, playerPos) +
                user.getName() + "님, " + " " + playerPos.getHumX() + " " + playerPos.getHumY() + " 에 둠으로서 이기셨어요. 축하드립니다. XD";
        channel.sendMessage(result);
    }

    public static void sendPlayerLose(Game game, Pos aiPos, IUser user, IChannel channel) {
        String result = TextDrawer.getGraphics(game, aiPos) +
                user.getName() + "님, " + " 제가 " + aiPos.getHumX() + " " + aiPos.getHumY() + " 에 둠으로서 지졌습니다. :/";
        channel.sendMessage(result);
    }

    public static void sendNextTurn(Game game, Pos aiPos, IUser user, IChannel channel) {
        String result = TextDrawer.getGraphics(game, aiPos) +
                user.getName() + "님, " + " 전 " + aiPos.getHumX() + " " + aiPos.getHumY() + " 에 뒀어요!";
        channel.sendMessage(result);
    }

    public static void notFoundGame(IUser user, IChannel channel) {
        channel.sendMessage(user.getName()+ "님, 하고계신 게임을 찾지 못했어요. !start 로 게임을 시작 해주세요!");
    }

    public static void sendSurrenPlayer(Game game, IUser user, IChannel channel) {
        String result = TextDrawer.getGraphics(game) +
                user.getName() + "님, 항복하셨네요. 제가 이겼습니다!";
        channel.sendMessage(result);
    }

}
