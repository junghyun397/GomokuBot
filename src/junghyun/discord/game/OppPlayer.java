package junghyun.discord.game;

public class OppPlayer {

    public enum PLAYER_TYPE {HUMAN, AI}
    private PLAYER_TYPE playerType;

    private String nameTag;

    public OppPlayer(PLAYER_TYPE playerType, String nameTag) {
        this.playerType = playerType;
        this.nameTag = nameTag;
    }

    public PLAYER_TYPE getPlayerType() {
        return this.playerType;
    }

    public String getNameTag() {
        return this.nameTag;
    }

}
