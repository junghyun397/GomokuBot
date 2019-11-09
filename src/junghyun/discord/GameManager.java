package junghyun.discord;

import junghyun.ai.Game;
import junghyun.discord.db.DBManager;
import junghyun.discord.db.Logger;
import junghyun.discord.game.ChatGame;
import junghyun.discord.game.OppPlayer;
import junghyun.discord.game.agent.GameAgent;
import junghyun.discord.game.agent.PVEGameAgent;
import junghyun.discord.game.agent.PVPGameAgent;
import junghyun.discord.ui.MessageAgent;
import junghyun.discord.ui.MessageManager;
import junghyun.discord.ui.graphics.TextDrawer;
import junghyun.ai.Pos;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameManager {

    final private static HashMap<Long, GameAgent> gameList = new HashMap<>();

    public static void bootGameManager() {
        Runnable task = GameManager::checkTimeOut;

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(task, Settings.TIMEOUT_CYCLE, Settings.TIMEOUT_CYCLE, TimeUnit.SECONDS);
    }

    private static void checkTimeOut() {
        long currentTime = System.currentTimeMillis();
        for (GameAgent game: GameManager.gameList.values().toArray(new GameAgent[0])) {
            if ((game.getChatGame().getUpdateTime() + Settings.TIMEOUT < currentTime)
                    && (game.getChatGame().getState() != ChatGame.STATE.TIMEOUT)) {
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

    public static void createGame(User user, TextChannel channel, User targetUser) {
        if (!GameManager.isHasGame(user.getIdLong())) {
            MessageManager.getInstance(channel.getGuild()).sendCreatGameFail(user, channel);
            return;
        }

        ChatGame chatGame;
        if (targetUser == null || targetUser.isBot()) chatGame = GameManager.createPVEGame(user, channel);
        else chatGame = GameManager.createPVPGame(user, targetUser, channel);

        Logger.loggerInfo("start game: " + chatGame.getNameTag()
                + " v. " + chatGame.getOppPlayer().getNameTag() + " : " + channel.getGuild().getName());
    }

    private static ChatGame createPVEGame(User user, TextChannel channel) {
        OppPlayer oppPlayer = new OppPlayer(OppPlayer.PLAYER_TYPE.AI, "AI", -1);
        ChatGame chatGame = new ChatGame(user.getIdLong(), new Game(), user.getName(), oppPlayer, user.getAvatarUrl());
        GameAgent gameAgent = new PVEGameAgent(chatGame);
        GameManager.putGame(user.getIdLong(), gameAgent);

        gameAgent.startGame(channel);
        return chatGame;
    }

    private static ChatGame createPVPGame(User user, User targetUser, TextChannel channel) {
        OppPlayer oppPlayer = new OppPlayer(OppPlayer.PLAYER_TYPE.HUMAN, targetUser.getName(), targetUser.getIdLong());
        ChatGame chatGame = new ChatGame(user.getIdLong(), new Game(), user.getName(), oppPlayer, user.getAvatarUrl());
        GameAgent gameAgent = new PVPGameAgent(chatGame);
        GameManager.putGame(user.getIdLong(), gameAgent);
        GameManager.putGame(targetUser.getIdLong(), gameAgent);

        gameAgent.startGame(channel);
        return chatGame;
    }

    public static void putStone(Pos pos, User user, TextChannel channel) {
        if (isHasGame(user.getIdLong())) {
            MessageManager.getInstance(channel.getGuild()).sendNotFoundGame(user, channel);
            return;
        }
        GameManager.getGame(user.getIdLong()).putStone(user, pos, channel);
    }

    public static void resignGame(User user, TextChannel channel) {
        if (isHasGame(user.getIdLong())) {
            MessageManager.getInstance(channel.getGuild()).sendNotFoundGame(user, channel);
            return;
        }
        GameManager.getGame(user.getIdLong()).resignGame(user, channel);
    }

    public static void endGame(ChatGame chatGame) {
        DBManager.saveGame(chatGame);
        GameManager.delGame(chatGame.getLongId());
        GameManager.postGame(chatGame);
        Logger.loggerInfo("end game: " +  chatGame.getNameTag() + " v. " + chatGame.getOppPlayer().getNameTag()
                + " " + chatGame.getGame().getTurns() + " " + chatGame.getState().toString());
        Logger.loggerInfo("canvas info\n"
                + TextDrawer.getGraphics(chatGame.getGame(), false));
    }

    private static void postGame(ChatGame chatGame) {
        if (chatGame.getState() != ChatGame.STATE.TIMEOUT && chatGame.getGame().getTurns() > 20) {
            MessageAgent.postResultOfficialChannel(chatGame, BotManager.getOfficialChannel());
        }
    }

    public static int getGameListSize() {
        return GameManager.gameList.size();
    }

}
