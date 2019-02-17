package junghyun.discord.game.agent;

import junghyun.ai.Pos;
import junghyun.discord.game.ChatGame;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public interface GameAgent {

    void startGame(IChannel channel);

    void putStone(IUser user, Pos pos, IChannel channel);

    void resignGame(IUser user, IChannel channel);

    void killGame();

    ChatGame getChatGame();

}
