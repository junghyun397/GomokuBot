package junghyun.discord.game.agent;

import junghyun.ai.Game;
import junghyun.ai.Pos;
import junghyun.discord.GameManager;
import junghyun.discord.game.ChatGame;
import junghyun.discord.ui.MessageManager;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.Random;

public class PVPGameAgent implements GameAgent {

    final private ChatGame chatGame;

    private boolean ownerColor;
    private boolean turnColor = true;

    public PVPGameAgent(ChatGame chatGame) {
        this.chatGame = chatGame;
    }

    @Override
    public void startGame(TextChannel channel) {
        this.ownerColor = new Random().nextBoolean();

        String textFAttack = chatGame.getNameTag();
        if (!this.ownerColor) textFAttack = chatGame.getOppPlayer().getNameTag();
        chatGame.getGame().setPlayerColor(this.ownerColor);

        MessageManager.getInstance(channel.getGuild()).sendCreateGameSuccess(chatGame, textFAttack, channel);
    }

    @Override
    public void putStone(User user, Pos pos, TextChannel channel) {
        Game game = chatGame.onUpdate().getGame();

        long nowPlayer = chatGame.getLongId();
        if (this.turnColor != this.ownerColor) nowPlayer = chatGame.getOppPlayer().getLongId();

        if (nowPlayer != user.getIdLong()) {
            String turnName = chatGame.getNameTag();
            if (nowPlayer == chatGame.getOppPlayer().getLongId()) {
                turnName = chatGame.getOppPlayer().getNameTag();
            }
            MessageManager.getInstance(channel.getGuild()).sendNotPlayerTurn(turnName,channel);
            return;
        }

        if (!game.canSetStone(pos.getX(), pos.getY())) {
            MessageManager.getInstance(channel.getGuild()).sendStoneAlreadyIn(chatGame, channel);
            return;
        }

        game.setStone(pos.getX(), pos.getY());
        if (game.isWin(pos.getX(), pos.getY(), !game.getColor())) {
            chatGame.setState(ChatGame.STATE.PVPWIN);

            String winPlayer = chatGame.getNameTag();
            String losePlayer = chatGame.getNameTag();
            if (user.getIdLong() == chatGame.getOppPlayer().getLongId()) {
                chatGame.getOppPlayer().setWin();
                winPlayer = chatGame.getOppPlayer().getNameTag();
            } else {
                losePlayer = chatGame.getOppPlayer().getNameTag();
            }
            MessageManager.getInstance(channel.getGuild()).sendPvPWin(chatGame, pos, winPlayer, losePlayer, channel);
            GameManager.endGame(chatGame);
            return;
        }

        if (game.isFull()) {
            chatGame.setState(ChatGame.STATE.FULL);
            MessageManager.getInstance(channel.getGuild()).sendFullCanvas(chatGame, channel);
            GameManager.endGame(chatGame);
            return;
        }

        String nowName = chatGame.getNameTag();
        String prvName = chatGame.getNameTag();
        if (nowPlayer != chatGame.getLongId()) {
            prvName = chatGame.getOppPlayer().getNameTag();
        } else {
            nowName = chatGame.getOppPlayer().getNameTag();
        }

        MessageManager.getInstance(channel.getGuild()).sendNextTurn(chatGame, pos, nowName, prvName, channel);
        this.turnColor = !this.turnColor;
    }

    @Override
    public void resignGame(User user, TextChannel channel) {
        chatGame.setState(ChatGame.STATE.RESIGN);

        String winPlayer = chatGame.getNameTag();
        String losePlayer = chatGame.getNameTag();

        if (user.getIdLong() == chatGame.getLongId()) winPlayer = chatGame.getOppPlayer().getNameTag();
        else losePlayer = chatGame.getOppPlayer().getNameTag();

        MessageManager.getInstance(channel.getGuild()).sendPvPResign(chatGame, winPlayer, losePlayer, channel);
        GameManager.endGame(chatGame);
    }

    @Override
    public void killGame() {
        GameManager.endGame(chatGame);
        GameManager.delGame(chatGame.getOppPlayer().getLongId());
    }

    @Override
    public ChatGame getChatGame() {
        return this.chatGame;
    }

}
