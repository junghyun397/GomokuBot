package junghyun.unit;

import junghyun.ai.Game;

public class ChatGame {

    private long longId;
    private String nameTag;

    private Game game;

    private long updateTime;

    public ChatGame(long longId, Game game, String nameTag) {
        this.longId = longId;
        this.nameTag = nameTag;
        this.game = game;
        this.updateTime = System.currentTimeMillis();
    }

    public long getLongId() {
        return longId;
    }

    public Game getGame() {
        return game;
    }

    public ChatGame onUpdate() {
        this.updateTime = System.currentTimeMillis();
        return this;
    }

    public long getUpdateTime() {
        return this.updateTime;
    }

    public String getNameTag() {
        return this.nameTag;
    }
}
