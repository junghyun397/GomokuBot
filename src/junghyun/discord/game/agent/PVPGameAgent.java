package junghyun.discord.game.agent;

import junghyun.ai.Game;
import junghyun.ai.Pos;
import junghyun.discord.GameManager;
import junghyun.discord.game.ChatGame;
import junghyun.discord.ui.MessageManager;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public class PVPGameAgent implements GameAgent {

    private ChatGame chatGame;

    private boolean playerColor = true;

    public PVPGameAgent(ChatGame chatGame) {
        this.chatGame = chatGame;
    }

    @Override
    public void startGame(IChannel channel) {

    }

    @Override
    public void putStone(IUser user, Pos pos, IChannel channel) {
        Game game = chatGame.onUpdate().getGame();

        if (!game.canSetStone(pos.getX(), pos.getY())) {
            MessageManager.getInstance(channel.getGuild()).sendAlreadyIn(chatGame, channel);
            return;
        }

        game.setStone(pos.getX(), pos.getY());
        if (game.isWin(pos.getX(), pos.getY(), game.getPlayerColor())) {
            chatGame.setState(ChatGame.STATE.PVPWIN);
            MessageManager.getInstance(channel.getGuild()).sendPvEWin(chatGame, pos, channel);
            GameManager.endGame(chatGame);
            return;
        }

        MessageManager.getInstance(channel.getGuild()).sendNextTurn(chatGame, pos, chatGame.getNameTag(), "AI", channel);
    }

    @Override
    public void resignGame(IUser user, IChannel channel) {
        chatGame.setState(ChatGame.STATE.RESIGN);

        String winPlayer = chatGame.getNameTag();
        String losePlayer = chatGame.getNameTag();

        if (user.getLongID() == chatGame.getLongId()) winPlayer = chatGame.getOppPlayer().getNameTag();
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
