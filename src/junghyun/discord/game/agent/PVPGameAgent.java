package junghyun.discord.game.agent;

import junghyun.ai.Game;
import junghyun.ai.Pos;
import junghyun.discord.GameManager;
import junghyun.discord.game.ChatGame;
import junghyun.discord.ui.MessageManager;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.Random;
import java.util.function.Consumer;

public class PVPGameAgent implements GameAgent {

    private final GameManager gameManager;
    private final MessageManager messageManager;

    private final ChatGame chatGame;

    private boolean ownerColor;
    private boolean turnColor = true;

    public PVPGameAgent(GameManager gameManager, MessageManager messageManager, ChatGame chatGame) {
        this.gameManager = gameManager;
        this.messageManager = messageManager;
        this.chatGame = chatGame;
    }

    @Override
    public void startGame(TextChannel channel) {
        this.ownerColor = new Random().nextBoolean();

        String textFAttack = chatGame.getNameTag();
        if (!this.ownerColor) textFAttack = chatGame.getOppPlayer().getNameTag();
        chatGame.getGame().setPlayerColor(this.ownerColor);

        this.messageManager.getAgent(channel.getGuild()).sendCreateGameSuccess(chatGame, textFAttack, channel);
    }

    @Override
    public void putStone(User user, Pos pos, TextChannel channel, Consumer<Boolean> then) {
        Game game = chatGame.onUpdate().getGame();

        long nowPlayer = chatGame.getLongId();
        if (this.turnColor != this.ownerColor) nowPlayer = chatGame.getOppPlayer().getLongId();

        if (nowPlayer != user.getIdLong()) {
            String turnName = chatGame.getNameTag();
            if (nowPlayer == chatGame.getOppPlayer().getLongId()) turnName = chatGame.getOppPlayer().getNameTag();

            then.accept(false);
            this.messageManager.getAgent(channel.getGuild()).sendNotPlayerTurn(turnName,channel);
            return;
        }

        if (!game.canSetStone(pos.getX(), pos.getY())) {
            then.accept(false);
            this.messageManager.getAgent(channel.getGuild()).sendStoneAlreadyIn(chatGame, channel);
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
            } else losePlayer = chatGame.getOppPlayer().getNameTag();
            this.gameManager.endGame(chatGame, channel);

            then.accept(true);
            this.messageManager.getAgent(channel.getGuild()).sendPvPWin(chatGame, pos, winPlayer, losePlayer, channel);
            return;
        }

        if (game.isFull()) {
            chatGame.setState(ChatGame.STATE.FULL);
            this.gameManager.endGame(chatGame, channel);

            then.accept(true);
            this.messageManager.getAgent(channel.getGuild()).sendFullCanvas(chatGame, channel);
            return;
        }

        String nowName = chatGame.getNameTag();
        String prvName = chatGame.getNameTag();
        if (nowPlayer != chatGame.getLongId()) prvName = chatGame.getOppPlayer().getNameTag();
        else nowName = chatGame.getOppPlayer().getNameTag();
        this.turnColor = !this.turnColor;

        then.accept(true);
        this.messageManager.getAgent(channel.getGuild()).sendNextTurn(chatGame, pos, nowName, prvName, channel);
    }

    @Override
    public void resignGame(User user, TextChannel channel) {
        chatGame.setState(ChatGame.STATE.RESIGN);

        String winPlayer = chatGame.getNameTag();
        String losePlayer = chatGame.getNameTag();

        if (user.getIdLong() == chatGame.getLongId()) winPlayer = chatGame.getOppPlayer().getNameTag();
        else losePlayer = chatGame.getOppPlayer().getNameTag();

        this.messageManager.getAgent(channel.getGuild()).sendPvPResign(chatGame, winPlayer, losePlayer, channel);
        this.gameManager.endGame(chatGame, channel);
        this.gameManager.delGame(chatGame.getOppPlayer().getLongId());
    }

    @Override
    public void killGame() {
        this.gameManager.endGame(chatGame, null);
        this.gameManager.delGame(chatGame.getOppPlayer().getLongId());
    }

    @Override
    public ChatGame getChatGame() {
        return this.chatGame;
    }

}
