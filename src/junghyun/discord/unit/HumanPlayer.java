package junghyun.discord.unit;

import junghyun.ai.Pos;
import junghyun.ai.engin.AIBase;

public class HumanPlayer extends OppPlayer {

    private long longId;

    public HumanPlayer(long longId, String nameTag) {
        super(PLAYER_TYPE.HUMAN, nameTag);
        this.longId = longId;
    }

}
