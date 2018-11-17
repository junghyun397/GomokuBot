package junghyun;

import junghyun.ai.Game;
import junghyun.ai.engin.AIBase;
import junghyun.db.DBManager;
import junghyun.db.Logger;
import junghyun.ui.Message;
import junghyun.unit.ChatGame;
import junghyun.unit.Pos;
import junghyun.unit.Settings;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class GameManager {

    private static HashMap<Long, ChatGame> gameList = new HashMap<>();

    static void bootGameManager() {
        Runnable task = GameManager::checkTimeOut;

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(task, Settings.TIMEOUT_CYCLE, Settings.TIMEOUT_CYCLE, TimeUnit.SECONDS);
    }

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

        ChatGame chatGame = new ChatGame(id, new Game(), user.getName());

        if (!playerColor) chatGame.getGame().setStone(7, 7, true);
        chatGame.getGame().setPlayerColor(playerColor);
        GameManager.putGame(chatGame);

        Logger.loggerInfo("Start Game: " + chatGame.getNameTag());
        Message.sendCreatedGame(chatGame.getGame(), playerColor, user, channel);
    }

    private static void endGame(ChatGame game) {
        DBManager.saveGame(game);
        GameManager.delGame(game.getLongId());
        Logger.loggerInfo("End Game: " + game.getNameTag());
    }

    private static void checkTimeOut() {
        long currentTime = System.currentTimeMillis();
        for (ChatGame game: (ChatGame[]) GameManager.gameList.entrySet().toArray()) {
            if (game.getUpdateTime()+Settings.TIMEOUT < currentTime) GameManager.endGame(game);
        }
    }

    static void resignGame(long id, IUser user, IChannel channel) {
        if (checkGame(id, user, channel)) return;
        Message.sendResignPlayer(getGame(id).getGame(), user, channel);
        GameManager.endGame(getGame(id));
    }

    static void putStone(long id, Pos pos, IUser user, IChannel channel) {
        ChatGame chatGame = GameManager.getGame(id).onUpdate();
        Game game = chatGame.getGame();

        if (!game.canSetStone(pos.getX(), pos.getY())) {
            Message.sendAlreadyIn(user, channel);
            return;
        }

        game.setStone(pos.getX(), pos.getY());
        if (game.isWin(pos.getX(), pos.getY(), game.getPlayerColor())) {
            chatGame.setWin();
            Message.sendPlayerWin(game, pos, user, channel);
            endGame(chatGame);
            return;
        }

        if (game.isFull()) {
            Message.sendFullCanvas(game, user, channel);
            endGame(chatGame);
            return;
        }

        Pos aiPos = new AIBase(game).getAiPoint();
        game.setStone(aiPos.getX(), aiPos.getY(), !game.getPlayerColor());
        if (game.isWin(aiPos.getX(), aiPos.getY(), !game.getPlayerColor())) {
            Message.sendPlayerLose(game, aiPos, user, channel);
            endGame(chatGame);
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
