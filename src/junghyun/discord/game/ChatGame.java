package junghyun.discord.game;

import junghyun.ai.Game;
import net.dv8tion.jda.api.entities.Message;

import java.util.LinkedList;
import java.util.List;

public class ChatGame {

    final private long longId;
    final private String nameTag;

    final private Game game;

    final private OppPlayer oppPlayer;

    final private String iconURL;

    public enum STATE {INP, WIN, PVPWIN, RESIGN, FULL, LOSE, TIMEOUT}
    private STATE state;

    private List<Message> msgList;
    private long updateTime;

    public ChatGame(long longId, Game game, String nameTag, OppPlayer oppPlayer, String iconURL) {
        this.longId = longId;
        this.game = game;
        this.nameTag = nameTag;
        this.oppPlayer = oppPlayer;
        this.iconURL = iconURL;

        this.state = STATE.INP;

        this.msgList = new LinkedList<>();

        this.updateTime = System.currentTimeMillis();
    }

    public void setState(STATE state) {
        this.state = state;
    }

    public long getLongId() {
        return longId;
    }

    public Game getGame() {
        return game;
    }

    public OppPlayer getOppPlayer() {
        return this.oppPlayer;
    }

    public void addMessage(Message iMessage) {
        this.msgList.add(iMessage);
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

    public boolean isWin() {
        return this.state == STATE.WIN;
    }

    public STATE getState() {
        return this.state;
    }

    public List<Message> getMessageList() {
        return this.msgList;
    }

    public String getIconURL() {
        return this.iconURL;
    }
}
