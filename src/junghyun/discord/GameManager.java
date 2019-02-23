package junghyun.discord;

import junghyun.ai.Game;
import junghyun.discord.db.DBManager;
import junghyun.discord.db.Logger;
import junghyun.discord.game.ChatGame;
import junghyun.discord.game.OppPlayer;
import junghyun.discord.game.agent.GameAgent;
import junghyun.discord.game.agent.PVEGameAgent;
import junghyun.discord.game.agent.PVPGameAgent;
import junghyun.discord.ui.MessageManager;
import junghyun.discord.ui.graphics.TextDrawer;
import junghyun.ai.Pos;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameManager {

    private static HashMap<Long, GameAgent> gameList = new HashMap<>();

    public static void bootGameManager() {
        Runnable task = GameManager::checkTimeOut;

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(task, Settings.TIMEOUT_CYCLE, Settings.TIMEOUT_CYCLE, TimeUnit.SECONDS);
    }

    private static void checkTimeOut() {
        long currentTime = System.currentTimeMillis();
        for (GameAgent game: GameManager.gameList.values().toArray(new GameAgent[0])) {
            if (game.getChatGame().getUpdateTime() + Settings.TIMEOUT < currentTime) {
                game.getChatGame().setState(ChatGame.STATE.TIMEOUT);
                game.killGame();
            }
        }
    }

    private static boolean isHasGame(long id) {
        return GameManager.getGame(id) == null;
    }

    private static void putGame(long id, GameAgent game) {
        GameManager.gameList.put(id, game);
    }

    private static GameAgent getGame(long id) {
        return GameManager.gameList.get(id);
    }

    public static void delGame(long id) {
        GameManager.gameList.remove(id);
    }

    public static void createGame(IUser user, IChannel channel, IUser targetUser) {
        if (!GameManager.isHasGame(user.getLongID())) {
            MessageManager.getInstance(channel.getGuild()).sendCreatGameFail(user, channel);
            return;
        }

        ChatGame chatGame;
        if (targetUser == null || targetUser.isBot()) chatGame = GameManager.createPVEGame(user, channel);
        else chatGame = GameManager.createPVPGame(user, targetUser, channel);

        Logger.loggerInfo("Start Game: " + chatGame.getNameTag() + " v. " + chatGame.getOppPlayer().getNameTag() + " : " + channel.getGuild().getName());
    }

    private static ChatGame createPVEGame(IUser user, IChannel channel) {
        OppPlayer oppPlayer = new OppPlayer(OppPlayer.PLAYER_TYPE.AI, "AI", -1);
        ChatGame chatGame = new ChatGame(user.getLongID(), new Game(), user.getName(), oppPlayer, user.getAvatarURL());
        GameAgent gameAgent = new PVEGameAgent(chatGame);
        GameManager.putGame(user.getLongID(), gameAgent);

        gameAgent.startGame(channel);
        return chatGame;
    }

    private static ChatGame createPVPGame(IUser user, IUser targetUser, IChannel channel) {
        OppPlayer oppPlayer = new OppPlayer(OppPlayer.PLAYER_TYPE.HUMAN, targetUser.getName(), targetUser.getLongID());
        ChatGame chatGame = new ChatGame(user.getLongID(), new Game(), user.getName(), oppPlayer, user.getAvatarURL());
        GameAgent gameAgent = new PVPGameAgent(chatGame);
        GameManager.putGame(user.getLongID(), gameAgent);
        GameManager.putGame(targetUser.getLongID(), gameAgent);

        gameAgent.startGame(channel);
        return chatGame;
    }

    public static void putStone(Pos pos, IUser user, IChannel channel) {
        if (isHasGame(user.getLongID())) {
            MessageManager.getInstance(channel.getGuild()).sendNotFoundGame(user, channel);
            return;
        }
        GameManager.getGame(user.getLongID()).putStone(user, pos, channel);
    }

    public static void resignGame(IUser user, IChannel channel) {
        if (isHasGame(user.getLongID())) {
            MessageManager.getInstance(channel.getGuild()).sendNotFoundGame(user, channel);
            return;
        }
        GameManager.getGame(user.getLongID()).resignGame(user, channel);
    }

    public static void endGame(ChatGame chatGame) {
        DBManager.saveGame(chatGame);
        GameManager.delGame(chatGame.getLongId());
        Logger.loggerInfo("End Game: " +  chatGame.getNameTag() + " v. " + chatGame.getOppPlayer().getNameTag() +
                " " + chatGame.getGame().getTurns() + " " + chatGame.getState().toString());
        Logger.loggerInfo("Canvas info ================================\n" + TextDrawer.getGraphics(chatGame.getGame(), false));
    }

    public static int getGameListSize() {
        return GameManager.gameList.size();
    }

}
