package junghyun.discord.game.agent;

import junghyun.ai.Game;
import junghyun.ai.Pos;
import junghyun.ai.engin.AIAgent;
import junghyun.discord.GameManager;
import junghyun.discord.game.ChatGame;
import junghyun.discord.ui.MessageManager;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.Random;
import java.util.function.Consumer;

public class PVEGameAgent implements GameAgent {

    private final GameManager gameManager;
    private final MessageManager messageManager;

    private final ChatGame chatGame;
    private final AIAgent aiAgent;

    public PVEGameAgent(GameManager gameManager, MessageManager messageManager, ChatGame chatGame) {
        this.gameManager = gameManager;
        this.messageManager = messageManager;
        this.chatGame = chatGame;

        this.aiAgent = new AIAgent(chatGame.getGame(), AIAgent.DIFF.MID);
    }

    @Override
    public void startGame(TextChannel channel) {
        int rColor = new Random().nextInt(3);
        boolean playerColor = rColor <= 0;

        String textFAttack = chatGame.getNameTag();
        if (!playerColor) {
            textFAttack = "AI";
            chatGame.getGame().setStone(7, 7, true);
        }
        chatGame.getGame().setPlayerColor(playerColor);

        this.messageManager.getAgent(channel.getGuild()).sendCreateGameSuccess(chatGame, textFAttack, channel);
    }

    @Override
    public void putStone(User user, Pos pos, TextChannel channel, Consumer<Boolean> then) {
        Game game = chatGame.getGame();

        if (!game.canSetStone(pos.getX(), pos.getY())) {
            then.accept(false);
            this.messageManager.getAgent(channel.getGuild()).sendStoneAlreadyIn(chatGame, channel);
            return;
        }

        game.setStone(pos.getX(), pos.getY());
        if (game.isWin(pos.getX(), pos.getY(), game.getPlayerColor())) {
            chatGame.setState(ChatGame.STATE.WIN);
            this.gameManager.endGame(chatGame, channel);

            then.accept(true);
            this.messageManager.getAgent(channel.getGuild()).sendPvEWin(chatGame, pos, channel);
            return;
        }

        if (game.isFull()) {
            chatGame.setState(ChatGame.STATE.FULL);
            chatGame.getOppPlayer().setWin();
            this.gameManager.endGame(chatGame, channel);

            then.accept(true);
            this.messageManager.getAgent(channel.getGuild()).sendFullCanvas(chatGame, channel);
            return;
        }

        Pos aiPos = this.aiAgent.getAiPoint();
        game.setStone(aiPos.getX(), aiPos.getY(), !game.getPlayerColor());
        if (game.isWin(aiPos.getX(), aiPos.getY(), !game.getPlayerColor())) {
            chatGame.setState(ChatGame.STATE.LOSE);
            chatGame.getOppPlayer().setWin();
            this.gameManager.endGame(chatGame, channel);

            then.accept(true);
            this.messageManager.getAgent(channel.getGuild()).sendPvELose(chatGame, aiPos, channel);
            return;
        }

        then.accept(true);
        this.messageManager.getAgent(channel.getGuild()).sendNextTurn(chatGame, aiPos, chatGame.getNameTag(), "AI", channel);
    }

    @Override
    public void resignGame(User user, TextChannel channel) {
        chatGame.setState(ChatGame.STATE.RESIGN);
        chatGame.getOppPlayer().setWin();
        this.messageManager.getAgent(channel.getGuild()).sendPvEResign(chatGame, channel);
        this.gameManager.endGame(chatGame, channel);
    }

    @Override
    public void killGame() {
        this.gameManager.endGame(chatGame, null);
    }

    @Override
    public ChatGame getChatGame() {
        return this.chatGame;
    }

}
