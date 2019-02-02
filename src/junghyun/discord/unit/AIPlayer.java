package junghyun.discord.unit;

import junghyun.ai.Game;
import junghyun.ai.Pos;
import junghyun.ai.engin.AIBase;

public class AIPlayer extends OppPlayer {

    private Game game;
    private AIBase.DIFF diff;

    public AIPlayer(Game game, AIBase.DIFF diff) {
        super(PLAYER_TYPE.HUMAN, "AI");
        this.diff = diff;
    }

}
