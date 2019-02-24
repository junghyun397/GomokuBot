package junghyun.discord.ui.languages;

public class LanguageCHN implements LanguageInterface {

    @Override
    public String LANGUAGE_CODE() {
        return "CHN";
    }

    @Override
    public String LANGUAGE_NAME() {
        return "简体中文:flag_cn:";
    }
    @Override
    public String LANGUAGE_DESCRIPTION() {
        return "请使用`~lang` `CHN`命令";
    }

    @Override
    public String HELP_INFO() {
        return "GomokuBot / 帮助";
    }
    @Override
    public String HELP_DESCRIPTION() {
        return "GomokuBot是一个在Discord上运行的开源五子棋Discord Bot。本开源程序所收集的棋谱将被用于强化学习模型训练。:)";
    }
    @Override
    public String HELP_DEV() {
        return "开发者";
    }
    @Override
    public String HELP_GIT() {
        return "Git 存储";
    }
    @Override
    public String HELP_VERSION() {
        return "版本";
    }
    @Override
    public String HELP_SUPPORT() {
        return "支援频道";
    }

    @Override
    public String HELP_CMD_INFO() {
        return "GomokuBot / 命令";
    }
    @Override
    public String HELP_CMD_HELP() {
        return "`~help` 帮助";
    }
    @Override
    public String HELP_CMD_LANG(String langList) {
        return "`~lang` " + langList + "改变在这个服务器上的语言设置。 Ex) `~lang` `ENG`";
    }
    @Override
    public String HELP_CMD_PVE() {
        return "`~start` 开始与人工智能的游戏。 ";
    }
    @Override
    public String HELP_CMD_PVP() {
        return "`~start` `@指定玩家` 与指定玩家开始游戏。 Ex) `~start` `@player`";
    }
    @Override
    public String HELP_CMD_RESIGN() {
        return "`~resign` 放弃现在正在进行的游戏。";
    }

    @Override
    public String RANK_INFO() {
        return "GomokuBot / 名次";
    }
    @Override
    public String RANK_DESCRIPTION() {
        return "从第一名到第十名的名次。 :D";
    }
    @Override
    public String RANK_WIN() {
        return "赢";
    }
    @Override
    public String RANK_LOSE() {
        return "输";
    }

    @Override
    public String LANG_CHANGE_ERROR() {
        return "语言制定错误！";
    }
    @Override
    public String LANG_SUCCESS() {
        return "改变语言设置为简体中文:flag_cn:！\n`Translated by`: `1,2,3,4-TetraMethylBenzene(Kawaii-cirno)`";
    }

    @Override
    public String GAME_NOT_FOUND(String nameTag) {
        return nameTag + "，无法找到现在进行中的游戏。请使用`~start`命令开始新游戏！";
    }
    @Override
    public String GAME_CREATE_FAIL(String nameTag) {
        return nameTag + "，无法生成新游戏。请结束现在的游戏。:thinking:";
    }
    @Override
    public String GAME_SYNTAX_FAIL(String nameTag) {
        return nameTag + "，您输入了错误的命令。请按照 `~s 字母 数字` 形式输入。:thinking:";
    }
    @Override
    public String GAME_ALREADY_IN(String nameTag) {
        return nameTag + "，那里已经有棋子了。 :thinking:";
    }

    @Override
    public String GAME_CREATE_INFO(String playerName, String targetName, String fAttack) {
        return "`" + playerName + "`与`" + targetName + "`的比赛已经开始！`" + fAttack + "`先下！";
    }
    @Override
    public String GAME_CMD_INFO() {
        return "请按照 `~s` `字母` `数字` 形式下棋。 Ex) `~s` `h` `8`";
    }

    @Override
    public String GAME_NEXT_TURN(String curName, String prvName, String lastPos) {
        return "`" + curName + "`，请下下一步棋。 `" + prvName + "`下在了 " + lastPos;
    }

    @Override
    public String GAME_PVP_TURN(String turnName) {
        return "现在是 `" + turnName + "`的顺序。请等待 `" + turnName + "`的下一步棋。:thinking:";
    }
    @Override
    public String GAME_PVP_WIN(String winName, String loseName, String lastPos) {
        return "`" + winName + "`以放在" + lastPos + "赢了`" + loseName + "`！";
    }
    @Override
    public String GAME_PVP_RESIGN(String winName, String loseName) {
        return "`" + loseName + "`以放弃游戏赢了`" + winName + "`！";
    }

    @Override
    public String GAME_PVP_INFO(String winName, String loseName, int winCount, int loseCount) {
        return winName + "与" + loseName + "的战绩更新为`" + winCount + " : " + loseCount + "`。";
    }

    @Override
    public String GAME_PVE_WIN(String lastPos) {
        return "您以落子在" + lastPos + "取得与人工智能的胜利！";
    }
    @Override
    public String GAME_PVE_LOSE(String lastPos) {
        return "人工智能以落子在" + lastPos + "而战败。";
    }
    @Override
    public String GAME_PVE_RESIGN() {
        return "以宣言放弃而战败给人工智能。";
    }

    @Override
    public String GAME_PVE_INFO(String playerName, int winCount, int loseCount, int rank) {
        return "与人工智能的战绩已更新为`" + winCount + " : " + loseCount + "`。现在 " + playerName + "的名次为第" + rank + "。";
    }

    @Override
    public String GAME_FULL() {
        return "因无处落子而自动记录为平局。";
    }

    @Override
    public String BOARD_INP() {
        return "正在进行";
    }
    @Override
    public String BOARD_FINISH() {
        return "已结束";
    }
    @Override
    public String BOARD_TURNS() {
        return "回合进行度";
    }
    @Override
    public String BOARD_TURN() {
        return "回合";
    }
    @Override
    public String BOARD_LOCATION() {
        return "最近落子位置";
    }

}