package junghyun.discord.ui.languages;

public class LanguageJPN extends LanguageENG{

    @Override
    public String LANGUAGE_CODE() {
        return "JPN";
    }

    @Override
    public String LANGUAGE_NAME() {
        return "日本語:flag_jp:";
    }
    @Override
    public String LANGUAGE_DESCRIPTION() {
        return "`~lang` `JPN` コマンドを使ってください。";
    }

    @Override
    public String HELP_INFO() {
        return "GomokuBot / ヘルプ";
    }
    @Override
    public String HELP_DESCRIPTION() {
        return "GomokuBotはDiscordで五目を楽しませてくれるオープンソースDiscord Botです。収集される基本データは強化学習モデル訓練に使われます。";
    }
    @Override
    public String HELP_DEV() {
        return "ディベロッパー";
    }
    @Override
    public String HELP_GIT() {
        return "Git 貯蔵所";
    }
    @Override
    public String HELP_VERSION() {
        return "バージョン";
    }
    @Override
    public String HELP_SUPPORT() {
        return "サポートチャンネル";
    }

    @Override
    public String HELP_CMD_INFO() {
        return "GomokuBot / コマンド";
    }
    @Override
    public String HELP_CMD_HELP() {
        return "`~help` ヘルプを案内します。";
    }
    @Override
    public String HELP_CMD_LANG(String langList) {
        return "`~lang` " + langList + "このサーバーで使われる言語設定を変更します。Ex) `~lang` `ENG`";
    }
    @Override
    public String HELP_CMD_PVE() {
        return "`~start` A.Iとゲームを開始します。";
    }
    @Override
    public String HELP_CMD_PVP() {
        return "`~start` `@言及` 言及されたプレーヤーとゲームを開始します。 Ex) `~start` `@player`";
    }
    @Override
    public String HELP_CMD_RESIGN() {
        return "`~resign` 現在進行中のゲームを諦めます。";
    }

    @Override
    public String RANK_INFO() {
        return "GomokuBot / ランキング";
    }
    @Override
    public String RANK_DESCRIPTION() {
        return "1位から10位までのランキングです。";
    }
    @Override
    public String RANK_WIN() {
        return "勝利";
    }
    @Override
    public String RANK_LOSE() {
        return "敗北";
    }

    @Override
    public String LANG_CHANGE_ERROR() {
        return "言語指定にエラーが発生しました。";
    }
    @Override
    public String LANG_SUCCESS() {
        return "言語設定を日本語:flag_jp:に変更しました\n`Translated by`: `S1RO`";
    }

    @Override
    public String GAME_NOT_FOUND(String nameTag) {
        return nameTag + "さん, 進行中のゲームがありません。`~start`でゲームを開始してください。";
    }
    @Override
    public String GAME_CREATE_FAIL(String nameTag) {
        return nameTag + "さん, ゲーム生成に失敗しました。今のゲームを終了してください。 :thinking:";
    }
    @Override
    public String GAME_SYNTAX_FAIL(String nameTag) {
        return nameTag + "さん, 間違いコマンドです。`~s アルファベット 数字` 形式に書いてください。 :thinking:";
    }
    @Override
    public String GAME_ALREADY_IN(String nameTag) {
        return nameTag + "さん, そこにはすでに碁石があります。 :thinking:";
    }

    @Override
    public String GAME_CREATE_INFO(String playerName, String targetName, String fAttack) {
        return "`" + playerName + "`と `" + targetName + "`のマッチが開始しました。先攻は `" + fAttack + "`です。";
    }
    @Override
    public String GAME_CMD_INFO() {
        return " `~s` `アルファベット` `数字` 形式に碁石を置いてください。 Ex) `~s` `h` `8`";
    }

    @Override
    public String GAME_NEXT_TURN(String curName, String prvName, String lastPos) {
        return "`" + curName + "`さん, 次の手を置いてください。 `" + prvName + "`は " + lastPos + "に置きました。";
    }

    @Override
    public String GAME_PVP_TURN(String turnName) {
        return "今は`" + turnName + "`'さんの番です。`" + turnName + "`さんの次の番を待ってください。 :thinking:";
    }
    @Override
    public String GAME_PVP_WIN(String winName, String loseName, String lastPos) {
        return "`" + winName + "`さんが " + lastPos + "に置いて`" + loseName + "`さんに勝ちました。";
    }
    @Override
    public String GAME_PVP_RESIGN(String winName, String loseName) {
        return "`" + loseName + "`さんが降伏を宣言して`" + winName + "`さんが勝ちました。";
    }

    @Override
    public String GAME_PVP_INFO(String winName, String loseName, int winCount, int loseCount) {
        return winName + "さんと" + loseName + "さんの戦績を`" + winCount + " : " + loseCount + "`にアップデートしました。";
    }

    @Override
    public String GAME_PVE_WIN(String lastPos) {
        return lastPos + "に置いてAIに勝ちました。おめでとうございます。";
    }
    @Override
    public String GAME_PVE_LOSE(String lastPos) {
        return "AIが" + lastPos + "に置いて負けました。";
    }
    @Override
    public String GAME_PVE_RESIGN() {
        return "降伏を宣言してAIに負けました。";
    }

    @Override
    public String GAME_PVE_INFO(String playerName, int winCount, int loseCount, int rank) {
        return "AI相手の戦績が`" + winCount + " : " + loseCount + "`にアップデートされました。現在" + playerName + "さんのランキングは" + rank + "位です。";
    }

    @Override
    public String GAME_FULL() {
        return "もう碁石を置く所が無くて無勝負に処理されました。";
    }

    @Override
    public String BOARD_INP() {
        return "進行中";
    }
    @Override
    public String BOARD_FINISH() {
        return "終了";
    }
    @Override
    public String BOARD_TURNS() {
        return "ターン進行度";
    }
    @Override
    public String BOARD_TURN() {
        return "ターン";
    }
    @Override
    public String BOARD_LOCATION() {
        return "最近着手位置";
    }

}
