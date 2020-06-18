package junghyun.discord.game.agent;

import junghyun.ai.Pos;
import junghyun.discord.game.ChatGame;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public interface GameAgent {

    void startGame(TextChannel channel);

    boolean putStone(User user, Pos pos, TextChannel channel);

    void resignGame(User user, TextChannel channel);

    void killGame();

    ChatGame getChatGame();

}
