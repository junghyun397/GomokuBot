package interact.i18n

class LanguageJPN : LanguageENG() {

    override fun languageCode(): String = "JPN"

    override fun languageName(): String = "日本語:flag_jp:"
    override fun languageDescription(): String = "`~lang` `JPN` コマンドを使ってください。"

    override fun helpInfo(): String = "GomokuBot / ヘルプ"
    override fun helpDescription(): String =
        "GomokuBotはDiscordで五目を楽しませてくれるオープンソースDiscord Botです。収集される基本データは強化学習モデル訓練に使われます。"
    override fun helpDeveloper(): String = "ディベロッパー"
    override fun helpRepository(): String = "Git 貯蔵所"
    override fun helpVersion(): String = "バージョン"
    override fun helpSupport(): String = "サポートチャンネル"

    override fun helpCommandInfo(): String = "GomokuBot / コマンド"
    override fun helpCommandHelp(): String = "`~help` ヘルプを案内します。"
    override fun helpCommandLang(langList: String): String =
        "`~lang` $langList このサーバーで使われる言語設定を変更します。Ex) `~lang` `ENG`"
    override fun helpCommandPVE(): String = "`~start` A.Iとゲームを開始します。"
    override fun helpCommandPVP(): String = "`~start` `@言及` 言及されたプレーヤーとゲームを開始します。 Ex) `~start` `@player`"
    override fun helpCommandResign(): String = "`~resign` 現在進行中のゲームを諦めます。"

    override fun rankInfo(): String = "GomokuBot / ランキング"
    override fun rankDescription(): String = "1位から10位までのランキングです。"
    override fun rankWin(): String = "勝利"
    override fun rankLose(): String = "敗北"

    override fun langUpdateError(): String = "言語指定にエラーが発生しました。"
    override fun langUpdateSuccess(): String = "言語設定を日本語:flag_jp:に変更しました\n`Translated by`: `S1RO`"

    override fun gameNotFound(nameTag: String): String =
        "$nameTag さん, 進行中のゲームがありません。`~start`でゲームを開始してください。"
    override fun gameAlreadyInProgress(nameTag: String): String =
        "$nameTag さん, ゲーム生成に失敗しました。今のゲームを終了してください。 :thinking:"
    override fun gameSyntaxFail(nameTag: String): String =
        "$nameTag さん, 間違いコマンドです。`~s アルファベット 数字` 形式に書いてください。 :thinking:"
    override fun gameAlreadyExits(nameTag: String): String = "$nameTag さん, そこにはすでに碁石があります。 :thinking:"

    override fun gameCreatedInfo(playerName: String, targetName: String, fAttack: String): String =
        "`$playerName`と `$targetName`のマッチが開始しました。先攻は `$fAttack`です。"
    override fun gameCommandInfo(): String = " `~s` `アルファベット` `数字` 形式に碁石を置いてください。 Ex) `~s` `h` `8`"

    override fun gameNextTurn(curName: String, prvName: String, lastPos: String): String =
        "`$curName`さん, 次の手を置いてください。 `$prvName`は $lastPos に置きました。"

    override fun gamePVPPleaseWait(turnName: String): String =
        "今は`$turnName`'さんの番です。`$turnName`さんの次の番を待ってください。 :thinking:"
    override fun gamePVPWin(winName: String, loseName: String, lastPos: String): String =
        "`$winName`さんが $lastPos に置いて`$loseName`さんに勝ちました。"
    override fun gamePVPWinCausedByResign(winName: String, loseName: String): String =
        "`$loseName`さんが降伏を宣言して`$winName`さんが勝ちました。"

    override fun gamePVPInfo(winName: String, loseName: String, winCount: Int, loseCount: Int): String =
        "$winName さんと$loseName さんの戦績を`$winCount : $loseCount`にアップデートしました。"

    override fun gamePVEWin(lastPos: String): String = lastPos + "に置いてAIに勝ちました。おめでとうございます。"
    override fun gamePVELose(lastPos: String): String = "AIが" + lastPos + "に置いて負けました。"
    override fun gamePVEResign(): String = "降伏を宣言してAIに負けました。"

    override fun gamePVEInfo(playerName: String, winCount: Int, loseCount: Int, rank: Int): String =
        "AI相手の戦績が`$winCount : $loseCount`にアップデートされました。現在$playerName さんのランキングは$rank 位です。"

    override fun gameTieCausedByFull(): String = "もう碁石を置く所が無くて無勝負に処理されました。"

    override fun boardInProgress(): String = "進行中"
    override fun boardFinish(): String = "終了"
    override fun boardProgress(): String = "ターン進行度"
    override fun boardTurns(): String = "ターン"
    override fun boardLocation(): String = "最近着手位置"

}