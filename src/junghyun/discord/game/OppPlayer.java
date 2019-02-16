package junghyun.discord.game;

public class OppPlayer {

    public enum PLAYER_TYPE {HUMAN, AI}
    private PLAYER_TYPE playerType;

    private String nameTag;
    private long longId;

    public OppPlayer(PLAYER_TYPE playerType, String nameTag, long longId) {
        this.playerType = playerType;
        this.nameTag = nameTag;
        this.longId = longId;
    }

    public PLAYER_TYPE getPlayerType() {
        return this.playerType;
    }

    public String getNameTag() {
        return this.nameTag;
    }

    public long getLongId() {
        return this.longId;
    }

}
