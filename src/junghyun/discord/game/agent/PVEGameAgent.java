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

public class PVEGameAgent implements GameAgent {

    final private ChatGame chatGame;
    final private AIAgent aiAgent;

    public PVEGameAgent(ChatGame chatGame) {
        this.chatGame = chatGame;
        this.aiAgent = new AIAgent(chatGame.getGame(), AIAgent.DIFF.MID);
    }

    @Override
    public void startGame(TextChannel channel) {
        int rColor = new Random().nextInt(3);
        boolean playerColor = true;
        if (rColor > 0) playerColor = false;

        String textFAttack = chatGame.getNameTag();
        if (!playerColor) {
            textFAttack = "AI";
            chatGame.getGame().setStone(7, 7, true);
        }
        chatGame.getGame().setPlayerColor(playerColor);

        MessageManager.getInstance(channel.getGuild()).sendCreateGameSuccess(chatGame, textFAttack, channel);
    }

    @Override
    public void putStone(User user, Pos pos, TextChannel channel) {
        Game game = chatGame.getGame();

        if (!game.canSetStone(pos.getX(), pos.getY())) {
            MessageManager.getInstance(channel.getGuild()).sendStoneAlreadyIn(chatGame, channel);
            return;
        }

        game.setStone(pos.getX(), pos.getY());
        if (game.isWin(pos.getX(), pos.getY(), game.getPlayerColor())) {
            chatGame.setState(ChatGame.STATE.WIN);
            MessageManager.getInstance(channel.getGuild()).sendPvEWin(chatGame, pos, channel);
            GameManager.endGame(chatGame);
            return;
        }

        if (game.isFull()) {
            chatGame.setState(ChatGame.STATE.FULL);
            chatGame.getOppPlayer().setWin();
            MessageManager.getInstance(channel.getGuild()).sendFullCanvas(chatGame, channel);
            GameManager.endGame(chatGame);
            return;
        }

        Pos aiPos = this.aiAgent.getAiPoint();
        game.setStone(aiPos.getX(), aiPos.getY(), !game.getPlayerColor());
        if (game.isWin(aiPos.getX(), aiPos.getY(), !game.getPlayerColor())) {
            chatGame.setState(ChatGame.STATE.LOSE);
            chatGame.getOppPlayer().setWin();
            MessageManager.getInstance(channel.getGuild()).sendPvELose(chatGame, aiPos, channel);
            GameManager.endGame(chatGame);
            return;
        }

        MessageManager.getInstance(channel.getGuild()).sendNextTurn(chatGame, aiPos, chatGame.getNameTag(), "AI", channel);
    }

    @Override
    public void resignGame(User user, TextChannel channel) {
        chatGame.setState(ChatGame.STATE.RESIGN);
        MessageManager.getInstance(channel.getGuild()).sendPvEResign(chatGame, channel);
        GameManager.endGame(chatGame);
    }

    @Override
    public void killGame() {
        GameManager.endGame(chatGame);
    }

    @Override
    public ChatGame getChatGame() {
        return this.chatGame;
    }

}
