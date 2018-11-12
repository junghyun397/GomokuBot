package junghyun;

import junghyun.ai.Game;
import junghyun.ai.engin.AIBase;
import junghyun.ui.Message;
import junghyun.unit.ChatGame;
import junghyun.unit.Pos;
import junghyun.unit.Settings;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.HashMap;
import java.util.Random;

import static java.lang.System.currentTimeMillis;

class GameManager {

    private static HashMap<Long, ChatGame> gameList = new HashMap<>();

    private static void putGame(ChatGame chatGame) {
        GameManager.gameList.put(chatGame.getLongId(), chatGame);
    }

    private static ChatGame getGame(long id) {
        return GameManager.gameList.get(id);
    }

    private static void delGame(long id) {
        GameManager.gameList.remove(id);
    }

    static void createGame(long id, IUser user, IChannel channel) {
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
        GameManager.putGame(chatGame);

        Message.sendCreatedGame(chatGame.getGame(), playerColor, user, channel);
    }

    private static void endGame(long id) {
        delGame(id);
    }

    private static void checkTimeOut() {
        long currentTime = System.currentTimeMillis();
        for (ChatGame game: (ChatGame[]) GameManager.gameList.entrySet().toArray()) {
            if (game.getUpdateTime()+Settings.TIMEOUT < currentTime) {
                GameManager.endGame(game.getLongId());
            }
        }
    }

    static void surrenGame(long id, IUser user, IChannel channel) {
        if (checkGame(id, user, channel)) return;
        Message.sendSurrenPlayer(getGame(id).getGame(), user, channel);
        GameManager.endGame(id);
    }

    static void putStone(long id, Pos pos, IUser user, IChannel channel) {
        Game game = getGame(id).onUpdate().getGame();

        if (!game.canSetStone(pos.getX(), pos.getY())) {
            Message.sendAlreadyIn(user, channel);
            return;
        }

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
            return true;
        }
        return false;
    }

}
