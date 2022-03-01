package junghyun.discord.ui.languages;

public class LanguageSKO extends LanguageKOR {

    @Override
    public String LANGUAGE_CODE() {
        return "SKO";
    }

    @Override
    public String LANGUAGE_NAME() {
        return "國漢文混用體:flag_kr:";
    }
    @Override
    public String LANGUAGE_DESCRIPTION() {
        return "`~lang` `SKO` 命令語를使用해주세요.";
    }

    @Override
    public String HELP_INFO() {
        return "GomokuBot / 助言";
    }
    @Override
    public String HELP_DESCRIPTION() {
        return "GomokuBot은Discord에서五目을滿喫할수있게해주는資料開放DiscordBot입니다. 收集된棋譜資料는强化學習模型訓鍊에使用됩니다. :)";
    }
    @Override
    public String HELP_DEV() {
        return "開發者";
    }
    @Override
    public String HELP_GIT() {
        return "Git 貯藏所";
    }
    @Override
    public String HELP_VERSION() {
        return "版";
    }
    @Override
    public String HELP_SUPPORT() {
        return "支援手段";
    }

    @Override
    public String HELP_CMD_INFO() {
        return "GomokuBot / 命令語";
    }
    @Override
    public String HELP_CMD_HELP() {
        return "`~help` 助言을告知해드립니다.";
    }
    @Override
    public String HELP_CMD_LANG(String langList) {
        return "`~lang` " + langList + "此奉仕機에서使用되는言語設定을改變합니다. Ex) `~lang` `ENG`";
    }
    @Override
    public String HELP_CMD_PVE() {
        return "`~start` 人工知能와의遊戱을開始합니다.";
    }
    @Override
    public String HELP_CMD_PVP() {
        return "`~start` `@言及` 言及된遊戱者와의遊戱을開始합니다. Ex) `~start` `@player`";
    }
    @Override
    public String HELP_CMD_RESIGN() {
        return "`~resign` 現在進行하고있는遊戱을抛棄합니다.";
    }

    @Override
    public String RANK_INFO() {
        return "GomokuBot / 順位";
    }
    @Override
    public String RANK_DESCRIPTION() {
        return "一位부터十位까지의順位입니다. :D";
    }
    @Override
    public String RANK_WIN() {
        return "勝利";
    }
    @Override
    public String RANK_LOSE() {
        return "敗北";
    }

    @Override
    public String LANG_CHANGE_ERROR() {
        return "言語指定에誤謬가有합니다!";
    }
    @Override
    public String LANG_SUCCESS() {
        return "言語設定이韓國語:flag_kr:로改變했습니다!\n`Translated by`: `1,2,3,4-TetraMethylBenzene(Kawaii-cirno)`";
    }

    @Override
    public String GAME_NOT_FOUND(String nameTag) {
        return nameTag + "님, 進行중인遊戱을찾을수없습니다. `~start`命令語로遊戱을開始해주세요!";
    }
    @Override
    public String GAME_CREATE_FAIL(String nameTag) {
        return nameTag + "님, 遊戱生成에失敗했습니다. 滿喫하고계신遊戱를完了해주세요. :thinking:";
    }
    @Override
    public String GAME_SYNTAX_FAIL(String nameTag) {
        return nameTag + "님, 彼는不正한命令語입니다. `~s 洋文 數字`形式으로記入하여주세요. :thinking:";
    }
    @Override
    public String GAME_ALREADY_IN(String nameTag) {
        return nameTag + "님, 彼地에는이미棋가置하여있습니다. :thinking:";
    }

    @Override
    public String GAME_CREATE_INFO(String playerName, String targetName, String fAttack) {
        return "`" + playerName + "`님과의對決이開始되었습니다! 先攻은`" + fAttack + "`님입니다.";
    }
    @Override
    public String GAME_CMD_INFO() {
        return " `~s` `洋文` `數字`型式으로棋를置하여주세요. Ex) `~s` `h` `8`";
    }

    @Override
    public String GAME_NEXT_TURN(String curName, String prvName, String lastPos) {
        return "`" + curName + "`님, 다음手를置하여주세요. `" + prvName + "`는 " + lastPos + "에置하였습니다.";
    }

    @Override
    public String GAME_PVP_TURN(String turnName) {
        return "只今은`" + turnName + "`님의次例입니다. `" + turnName + "`님의다음手를待機해주세요. :thinking:";
    }
    @Override
    public String GAME_PVP_WIN(String winName, String loseName, String lastPos) {
        return "`" + winName + "`님이" + lastPos + "에置함으로서`" + loseName + "`님에게勝利하였습니다!";
    }
    @Override
    public String GAME_PVP_RESIGN(String winName, String loseName) {
        return "`" + loseName + "`님이降伏을宣言함으로서`" + winName + "`님이勝利하였습니다!";
    }

    @Override
    public String GAME_PVP_INFO(String winName, String loseName, int winCount, int loseCount) {
        return winName + "님과" + loseName + "님의戰績이`" + winCount + " : " + loseCount + "`로更新되었습니다.";
    }

    @Override
    public String GAME_PVE_WIN(String lastPos) {
        return lastPos +  "에置함으로서人工知能에게勝利하셨습니다. 祝賀합니다!";
    }
    @Override
    public String GAME_PVE_LOSE(String lastPos) {
        return "人工知能이" + lastPos + "에置함으로서敗北하셨습니다.";
    }
    @Override
    public String GAME_PVE_RESIGN() {
        return "降伏을宣言함으로서人工知能에게敗北하였습니다.";
    }

    @Override
    public String GAME_PVE_INFO(String playerName, int winCount, int loseCount, int rank) {
        return "人工知能相對戰績이`" + winCount + " : " + loseCount + "`로更新되었습니다. 現在" + playerName + "님의順位는" + rank + "位입니다.";
    }

    @Override
    public String GAME_FULL() {
        return "더以上棋를置할位가無하여無勝負處理되었습니다.";
    }

    @Override
    public String BOARD_INP() {
        return "進行中";
    }
    @Override
    public String BOARD_FINISH() {
        return "終了됨";
    }
    @Override
    public String BOARD_TURNS() {
        return "次例進行度";
    }
    @Override
    public String BOARD_TURN() {
        return "次例";
    }
    @Override
    public String BOARD_LOCATION() {
        return "最近着手位置";
    }

}