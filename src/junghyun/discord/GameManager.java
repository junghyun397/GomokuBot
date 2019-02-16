package junghyun.discord;

import junghyun.ai.Game;
import junghyun.ai.engin.AIBase;
import junghyun.discord.db.DBManager;
import junghyun.discord.db.Logger;
import junghyun.discord.game.AIPlayer;
import junghyun.discord.game.ChatGame;
import junghyun.discord.game.HumanPlayer;
import junghyun.discord.game.OppPlayer;
import junghyun.discord.ui.MessageManager;
import junghyun.discord.ui.TextDrawer;
import junghyun.discord.unit.*;
import junghyun.ai.Pos;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameManager {

    private static HashMap<Long, ChatGame> gameList = new HashMap<>();

    public static void bootGameManager() {
        Runnable task = GameManager::checkTimeOut;

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(task, Settings.TIMEOUT_CYCLE, Settings.TIMEOUT_CYCLE, TimeUnit.SECONDS);
    }

    private static void checkTimeOut() {
        long currentTime = System.currentTimeMillis();
        for (ChatGame game: GameManager.gameList.values().toArray(new ChatGame[0])) {
            if (game.getUpdateTime() + Settings.TIMEOUT < currentTime) {
                game.setState(ChatGame.STATE.TIMEOUT);
                GameManager.endGame(game);
            }
        }
    }

    private static boolean checkGame(long id) {
        return GameManager.getGame(id) == null;
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

    public static void createGame(IUser user, IChannel channel, IUser targetUser) {
        if (GameManager.checkGame(user.getLongID())) {
            MessageManager.getInstance(channel.getGuild()).sendFailCreatedGame(user, channel);
            return;
        }

        ChatGame chatGame;
        if (targetUser == null) chatGame = GameManager.createPVEGame(user.getLongID(), user);
        else chatGame = GameManager.createPVPGame(user.getLongID(), user, targetUser);

        GameManager.putGame(chatGame);
        Logger.loggerInfo("Start Game: " + chatGame.getNameTag() + " v. " + chatGame.getOppPlayer().getNameTag());
    }

    private static ChatGame createPVEGame(long id, IUser user) {
        Game game = new Game();
        int rColor = new Random().nextInt(3);
        boolean playerColor = true;
        if (rColor > 0) playerColor = false;

        OppPlayer AIPlayer = new AIPlayer(game, AIBase.DIFF.MID);
        ChatGame chatGame = new ChatGame(id, game, user.getName(), AIPlayer);

        if (!playerColor) chatGame.getGame().setStone(7, 7, true);
        chatGame.getGame().setPlayerColor(playerColor);

        return chatGame;
    }

    private static ChatGame createPVPGame(long id, IUser user, IUser targetUser) {
        Game game = new Game();
        boolean rColor = new Random().nextBoolean();

        OppPlayer HumanPlayer = new HumanPlayer(targetUser.getLongID(), targetUser.getName());

        return new ChatGame(id, game, user.getName(), HumanPlayer);
    }

    private static void endGame(ChatGame chatGame) {
        if (chatGame.getOppPlayer().getPlayerType() == OppPlayer.PLAYER_TYPE.AI) DBManager.saveGame(chatGame);
        GameManager.delGame(chatGame.getLongId());
        Logger.loggerInfo("End Game: " +  chatGame.getNameTag() + " v. " + chatGame.getOppPlayer().getNameTag() +
                " " + chatGame.getGame().getTurns() + " " + chatGame.getState().toString());
        Logger.loggerInfo("Canvas info ================================\n" +
                TextDrawer.getGraphics(chatGame.getGame(), false));
    }

    static void resignGame(IUser user, IChannel channel) {
        if (GameManager.checkGame(user.getLongID())) return;
        ChatGame chatGame = GameManager.getGame(user.getLongID());
        chatGame.setState(ChatGame.STATE.RESIGN);
        MessageManager.getInstance(channel.getGuild()).sendResignPlayer(chatGame, user, channel);
        GameManager.endGame(chatGame);
    }

    static void putStone(long id, Pos pos, IUser user, IChannel channel) {
        ChatGame chatGame = GameManager.getGame(id);
        if (chatGame == null) return;
        Game game = chatGame.onUpdate().getGame();

        if (!game.canSetStone(pos.getX(), pos.getY())) {
            MessageManager.getInstance(channel.getGuild()).sendAlreadyIn(chatGame, user, channel);
            return;
        }

        game.setStone(pos.getX(), pos.getY());
        if (game.isWin(pos.getX(), pos.getY(), game.getPlayerColor())) {
            chatGame.setState(ChatGame.STATE.WIN);
            MessageManager.getInstance(channel.getGuild()).sendPlayerWin(chatGame, pos, user, channel);
            endGame(chatGame);
            return;
        }

        if (game.isFull()) {
            chatGame.setState(ChatGame.STATE.FULL);
            MessageManager.getInstance(channel.getGuild()).sendFullCanvas(chatGame, user, channel);
            endGame(chatGame);
            return;
        }

        Pos aiPos = new AIBase(game, AIBase.DIFF.MID).getAiPoint();
        game.setStone(aiPos.getX(), aiPos.getY(), !game.getPlayerColor());
        if (game.isWin(aiPos.getX(), aiPos.getY(), !game.getPlayerColor())) {
            chatGame.setState(ChatGame.STATE.LOSE);
            MessageManager.getInstance(channel.getGuild()).sendPlayerLose(chatGame, aiPos, user, channel);
            endGame(chatGame);
            return;
        }

        MessageManager.getInstance(channel.getGuild()).sendNextTurn(chatGame, aiPos, user, channel);
    }

    public static int getGameListSize() {
        return GameManager.gameList.size();
    }

}
