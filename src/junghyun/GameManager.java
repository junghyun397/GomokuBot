package junghyun;

import junghyun.ai.Game;
import junghyun.ai.engin.AIBase;
import junghyun.ui.Message;
import junghyun.unit.ChatGame;
import junghyun.unit.Pos;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.HashMap;
import java.util.Random;

public class GameManager {

    private static HashMap<Long, ChatGame> gameList = new HashMap<>();

    private static void putGame(ChatGame chatGame) {
        gameList.put(chatGame.getLongId(), chatGame);
    }

    private static ChatGame getGame(long id) {
        return gameList.get(id);
    }

    private static void delGame(long id) {
        gameList.remove(id);
    }

    public static void createGame(long id, IUser user, IChannel channel) {
        if (getGame(id) != null) {
            Message.sendFailCreatedGame(user, channel);
            return;
        }

        int rColor = new Random().nextInt(3);
        boolean playerColor = true;
        if (rColor > 0) playerColor = false;

        ChatGame chatGame = new ChatGame(id, new Game());

        if (!playerColor) chatGame.getGame().setStone(7, 7, true);
        chatGame.getGame().setPlayerColor(playerColor);
        putGame(chatGame);

        Message.sendCreatedGame(chatGame.getGame(), playerColor, user, channel);
    }

    private static void endGame(long id) {
        delGame(id);
    }

    public static void surrenGame(long id, IUser user, IChannel channel) {
        if (!checkGame(id, user, channel)) return;
        Message.sendSurrenPlayer(getGame(id).getGame(), user, channel);
        endGame(id);
    }

    public static void putStone(long id, Pos pos, IUser user, IChannel channel) {
        if (!checkGame(id, user, channel)) return;

        Game game = getGame(id).getGame();

        if (!game.canSetStone(pos.getX(), pos.getY())) return;

        game.setStone(pos.getX(), pos.getY());
        if (game.isWin(pos.getX(), pos.getY(), game.getPlayerColor())) {
            Message.sendPlayerWin(game, pos, user, channel);
            endGame(id);
            return;
        }

        Pos aiPos = new AIBase(game).getAiPoint();
        game.setStone(aiPos.getX(), aiPos.getY(), !game.getPlayerColor());
        if (game.isWin(aiPos.getX(), aiPos.getY(), !game.getPlayerColor())) {
            Message.sendPlayerLose(game, aiPos, user, channel);
            endGame(id);
            return;
        }

        Message.sendNextTurn(game, aiPos, user, channel);
    }

    private static boolean checkGame(long id, IUser user, IChannel channel) {
        if (getGame(id) == null) {
            Message.notFoundGame(user, channel);
            return false;
        }
        return true;
    }

}
