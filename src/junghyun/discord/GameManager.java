package junghyun.discord;

import junghyun.ai.Game;
import junghyun.ai.Pos;
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
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class GameManager {

    private final Logger logger;
    private final BotManager botManager;
    private final DBManager dbManager;
    private final MessageManager messageManager;

    private final HashMap<Long, GameAgent> gameList = new HashMap<>();

    public GameManager(BotManager botManager, DBManager dbManager, Logger logger, MessageManager messageManager) {
        this.botManager = botManager;
        this.dbManager = dbManager;
        this.logger = logger;
        this.messageManager = messageManager;
    }

    public void startGameManager() {
        Runnable task = this::checkTimeOut;

        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(task, Settings.TIMEOUT_CYCLE, Settings.TIMEOUT_CYCLE, TimeUnit.SECONDS);
    }

    private void checkTimeOut() {
        long currentTime = System.currentTimeMillis();
        for (GameAgent game: this.gameList.values().toArray(new GameAgent[0])) {
            if ((game.getChatGame().getUpdateTime() + Settings.TIMEOUT < currentTime)
                    && (game.getChatGame().getState() != ChatGame.STATE.TIMEOUT)) {
                game.getChatGame().setState(ChatGame.STATE.TIMEOUT);
                game.killGame();
            }
        }
    }

    private boolean isHasGame(long id) {
        return this.getGame(id) == null;
    }

    private void putGame(long id, GameAgent game) {
        this.gameList.put(id, game);
    }

    private GameAgent getGame(long id) {
        return this.gameList.get(id);
    }

    public void delGame(long id) {
        this.gameList.remove(id);
    }

    public void createGame(User user, TextChannel channel, User targetUser, Consumer<Boolean> then) {
        if (!this.isHasGame(user.getIdLong())) {
            then.accept(false);
            this.messageManager.getAgent(channel.getGuild()).sendCreateGameFail(user, channel);
            return;
        }

        then.accept(true);

        ChatGame chatGame;
        if (targetUser == null || targetUser.isBot()) chatGame = this.createPVEGame(user, channel);
        else chatGame = this.createPVPGame(user, targetUser, channel);

        this.logger.loggerInfo("start game: " + chatGame.getNameTag()
                + " v. " + chatGame.getOppPlayer().getNameTag() + " : " + channel.getGuild().getName());
    }

    private ChatGame createPVEGame(User user, TextChannel channel) {
        OppPlayer oppPlayer = new OppPlayer(OppPlayer.PLAYER_TYPE.AI, "AI", -1);
        ChatGame chatGame = new ChatGame(user.getIdLong(), new Game(), user.getName(), oppPlayer, user.getAvatarUrl());
        GameAgent gameAgent = new PVEGameAgent(this, messageManager, chatGame);
        this.putGame(user.getIdLong(), gameAgent);

        gameAgent.startGame(channel);
        return chatGame;
    }

    private ChatGame createPVPGame(User user, User targetUser, TextChannel channel) {
        OppPlayer oppPlayer = new OppPlayer(OppPlayer.PLAYER_TYPE.HUMAN, targetUser.getName(), targetUser.getIdLong());
        ChatGame chatGame = new ChatGame(user.getIdLong(), new Game(), user.getName(), oppPlayer, user.getAvatarUrl());
        GameAgent gameAgent = new PVPGameAgent(this, messageManager, chatGame);
        this.putGame(user.getIdLong(), gameAgent);
        this.putGame(targetUser.getIdLong(), gameAgent);

        gameAgent.startGame(channel);
        return chatGame;
    }

    public void putStone(Pos pos, User user, TextChannel channel, Consumer<Boolean> then) {
        if (isHasGame(user.getIdLong())) {
            then.accept(false);
            this.messageManager.getAgent(channel.getGuild()).sendNotFoundGame(user, channel);
            return;
        }

        this.getGame(user.getIdLong()).putStone(user, pos, channel, then);
    }

    public void resignGame(User user, TextChannel channel, Consumer<Boolean> then) {
        if (isHasGame(user.getIdLong())) {
            then.accept(false);
            this.messageManager.getAgent(channel.getGuild()).sendNotFoundGame(user, channel);
            return;
        }

        then.accept(true);
        this.getGame(user.getIdLong()).resignGame(user, channel);
    }

    public void endGame(ChatGame chatGame, TextChannel channel) {
        this.dbManager.saveGame(chatGame);
        this.delGame(chatGame.getLongId());
        this.postGame(chatGame, channel);
        this.logger.loggerInfo("end game: " +  chatGame.getNameTag() + " v. " + chatGame.getOppPlayer().getNameTag()
                + " " + chatGame.getGame().getTurns() + " " + chatGame.getState().toString());
        this.logger.loggerInfo("canvas info\n"
                + TextDrawer.getConsoleGraphics(chatGame.getGame(), true));
    }

    @SuppressWarnings("unused")
    private void postGame(ChatGame chatGame, TextChannel channel) {
        if (chatGame.getState() != ChatGame.STATE.TIMEOUT && chatGame.getGame().getTurns() > 20) {
            long id = MessageAgent.postResultOfficialChannel(chatGame, this.botManager.getOfficialChannel());
            // if (channel != null) MessageManager.getInstance(channel.getGuild()).sendPerfectGameArchived(chatGame.getNameTag(), channel, id);
        }
    }

    public int getGameListSize() {
        return this.gameList.size();
    }

}
