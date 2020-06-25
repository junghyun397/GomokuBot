package junghyun.discord.game.agent;

import junghyun.ai.Pos;
import junghyun.discord.game.ChatGame;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.function.Consumer;

public interface GameAgent {

    void startGame(TextChannel channel);

    void putStone(User user, Pos pos, TextChannel channel, Consumer<Boolean> then);

    void resignGame(User user, TextChannel channel);

    void killGame();

    ChatGame getChatGame();

}
