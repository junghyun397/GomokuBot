package junghyun.ui;

import junghyun.db.DBManager;
import junghyun.ui.languages.MessageEng;
import junghyun.ui.languages.MessageKor;
import junghyun.unit.ChatGame;
import junghyun.unit.Pos;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.util.HashMap;

public class MessageBase {

    public enum LANG {ERR, ENG, KOR}
    private static HashMap<Long, LANG> langList;

    public static void loadMessage() {
        MessageBase.langList = new HashMap<>();
        MessageEng.buildMessage();
        MessageKor.buildMessage();
    }

    public static LANG getLangByString(String str) {
        LANG lang = LANG.ERR;
        switch (str.toLowerCase()) {
            case "eng":
                lang = LANG.ENG;
                break;
            case "kor":
                lang = LANG.KOR;
                break;
        }
        return lang;
    }

    public static void setLanguage(long id, LANG lang) {
        MessageBase.langList.put(id, lang);
    }

    private static LANG getLanguage(long id) {
        LANG lang = MessageBase.langList.get(id);
        if (lang == null) {
            DBManager.GuildDataSet guildData = DBManager.getGuildData(id);
            if (guildData == null) MessageBase.langList.put(id, LANG.ENG);
            else MessageBase.langList.put(id, guildData.getLang());
        }
        return lang;
    }

    public static void sendHelp(IChannel channel) {
        switch (MessageBase.getLanguage(channel.getGuild().getLongID())) {
            case ENG:
                MessageEng.sendHelp(channel);
                break;
            case KOR:
                MessageKor.sendHelp(channel);
                break;
        }
    }

    public static void sendLanguageChangeInfo(IChannel channel) {
        MessageEng.sendLanguageChangeInfo(channel);
    }

    public static void sendRank(IUser user, IChannel channel, DBManager.UserDataSet[] rankData) {
        switch (MessageBase.getLanguage(channel.getGuild().getLongID())) {
            case ENG:
                MessageEng.sendRank(user, channel, rankData);
                break;
            case KOR:
                MessageKor.sendRank(user, channel, rankData);
                break;
        }
    }

    public static void sendLanguageChange(IChannel channel, LANG lang) {
        switch (MessageBase.getLanguage(channel.getGuild().getLongID())) {
            case ENG:
                MessageEng.sendLanguageChange(channel, lang);
                break;
            case KOR:
                MessageKor.sendLanguageChange(channel, lang);
                break;
        }
    }

    public static void sendCreatedGame(ChatGame chatGame, boolean playerColor, IUser user, IChannel channel) {
        switch (MessageBase.getLanguage(channel.getGuild().getLongID())) {
            case ENG:
                MessageEng.sendCreatedGame(chatGame, playerColor, user, channel);
                break;
            case KOR:
                MessageKor.sendCreatedGame(chatGame, playerColor, user, channel);
                break;
        }
    }

    public static void sendFailCreatedGame(IUser user, IChannel channel) {
        switch (MessageBase.getLanguage(channel.getGuild().getLongID())) {
            case ENG:
                MessageEng.sendFailCreatedGame(user, channel);
                break;
            case KOR:
                MessageKor.sendFailCreatedGame(user, channel);
                break;
        }
    }

    public static void sendErrorGrammarSet(IUser user, IChannel channel) {
        switch (MessageBase.getLanguage(channel.getGuild().getLongID())) {
            case ENG:
                MessageEng.sendErrorGrammarSet(user, channel);
                break;
            case KOR:
                MessageKor.sendErrorGrammarSet(user, channel);
                break;
        }
    }

    public static void sendAlreadyIn(ChatGame chatGame, IUser user, IChannel channel) {
        switch (MessageBase.getLanguage(channel.getGuild().getLongID())) {
            case ENG:
                MessageEng.sendAlreadyIn(chatGame, user, channel);
                break;
            case KOR:
                MessageKor.sendAlreadyIn(chatGame, user, channel);
                break;
        }
    }

    public static void sendPlayerWin(ChatGame chatGame, Pos playerPos, IUser user, IChannel channel) {
        switch (MessageBase.getLanguage(channel.getGuild().getLongID())) {
            case ENG:
                MessageEng.sendPlayerWin(chatGame, playerPos, user, channel);
                break;
            case KOR:
                MessageKor.sendPlayerWin(chatGame, playerPos, user, channel);
                break;
        }
    }

    public static void sendPlayerLose(ChatGame chatGame, Pos aiPos, IUser user, IChannel channel) {
        switch (MessageBase.getLanguage(channel.getGuild().getLongID())) {
            case ENG:
                MessageEng.sendPlayerLose(chatGame, aiPos, user, channel);
                break;
            case KOR:
                MessageKor.sendPlayerLose(chatGame, aiPos, user, channel);
                break;
        }
    }

    public static void sendNextTurn(ChatGame chatGame, Pos aiPos, IUser user, IChannel channel) {
        switch (MessageBase.getLanguage(channel.getGuild().getLongID())) {
            case ENG:
                MessageEng.sendNextTurn(chatGame, aiPos, user, channel);
                break;
            case KOR:
                MessageKor.sendNextTurn(chatGame, aiPos, user, channel);
                break;
        }
    }

    public static void notFoundGame(IUser user, IChannel channel) {
        switch (MessageBase.getLanguage(channel.getGuild().getLongID())) {
            case ENG:
                MessageEng.notFoundGame(user, channel);
                break;
            case KOR:
                MessageKor.notFoundGame(user, channel);
                break;
        }
    }

    public static void sendResignPlayer(ChatGame chatGame, IUser user, IChannel channel) {
        switch (MessageBase.getLanguage(channel.getGuild().getLongID())) {
            case ENG:
                MessageEng.sendResignPlayer(chatGame, user, channel);
                break;
            case KOR:
                MessageKor.sendResignPlayer(chatGame, user, channel);
                break;
        }
    }

    public static void sendFullCanvas(ChatGame chatGame, IUser user, IChannel channel) {
        switch (MessageBase.getLanguage(channel.getGuild().getLongID())) {
            case ENG:
                MessageEng.sendFullCanvas(chatGame, user, channel);
                break;
            case KOR:
                MessageKor.sendFullCanvas(chatGame, user, channel);
                break;
        }
    }

}
