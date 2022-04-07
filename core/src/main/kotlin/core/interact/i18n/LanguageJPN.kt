package core.interact.i18n

class LanguageJPN : LanguageENG() {

    override fun languageCode() = "JPN"

    override fun languageName() = "日本語\uD83C\uDDEF\uD83C\uDDF5"
    override fun languageSuggestion() = "`~lang` `JPN` コマンドを使ってください。"

    override fun helpAboutEmbedTitle() = "GomokuBot / ヘルプ"
    override fun helpAboutEmbedDescription() =
        "GomokuBotはDiscordで五目を楽しませてくれるオープンソースDiscord Botです。収集される基本データは強化学習モデル訓練に使われます。"
    override fun helpAboutEmbedDeveloper() = "ディベロッパー"
    override fun helpAboutEmbedRepository() = "Git 貯蔵所"
    override fun helpAboutEmbedVersion() = "バージョン"
    override fun helpAboutEmbedSupport() = "サポートチャンネル"

    override fun helpCommandEmbedTitle() = "GomokuBot / コマンド"
    override fun helpCommandEmbedHelp() = "`~help` ヘルプを案内します。"
    override fun helpCommandEmbedLang(langList: String) =
        "`~lang` $langList このサーバーで使われる言語設定を変更します。Ex) `~lang` `ENG`"
    override fun helpCommandEmbedStartPVE() = "`~start` A.Iとゲームを開始します。"
    override fun helpCommandEmbedStartPVP() = "`~start` `@言及` 言及されたプレーヤーとゲームを開始します。 Ex) `~start` `@player`"
    override fun helpCommandEmbedResign() = "`~resign` 現在進行中のゲームを諦めます。"

    override fun rankEmbedTitle() = "GomokuBot / ランキング"
    override fun rankEmbedDescription() = "1位から10位までのランキングです。"
    override fun rankEmbedWin() = "勝利"
    override fun rankEmbedLose() = "敗北"

    override fun languageUpdated() = "言語設定を日本語:flag_jp:に変更しました\n`Translated by`: `S1RO`"

    override fun startErrorSessionAlready(nameTag: String) =
        "$nameTag さん, ゲーム生成に失敗しました。今のゲームを終了してください。 :thinking:"
    override fun setErrorIllegalArgument() =
        "さん, 間違いコマンドです。`~s アルファベット 数字` 形式に書いてください。 :thinking:"
    override fun setErrorExist(nameTag: String, pos: String) = "$nameTag さん, そこにはすでに碁石があります。 :thinking:"

    override fun beginPVP(ownerName: String, opponentName: String, fMove: String) =
        "`$ownerName`と `$opponentName`のマッチが開始しました。先攻は `$fMove`です。"

    override fun processNext(curName: String, prvName: String, lastPos: String) =
        "`$curName`さん, 次の手を置いてください。 `$prvName`は $lastPos に置きました。"

    override fun processErrorOrder(turnName: String) =
        "今は`$turnName`'さんの番です。`$turnName`さんの次の番を待ってください。 :thinking:"
    override fun endPVPWin(winName: String, loseName: String, lastPos: String) =
        "`$winName`さんが $lastPos に置いて`$loseName`さんに勝ちました。"
    override fun endPVPResign(winName: String, loseName: String) =
        "`$loseName`さんが降伏を宣言して`$winName`さんが勝ちました。"

    override fun endPVEWin(latestPos: String) = latestPos + "に置いてAIに勝ちました。おめでとうございます。"
    override fun endPVELose(latestPos: String) = "AIが" + latestPos + "に置いて負けました。"
    override fun endPVEResign() = "降伏を宣言してAIに負けました。"

    override fun endPVPTie() = "もう碁石を置く所が無くて無勝負に処理されました。"

    override fun boardInProgress() = "進行中"
    override fun boardFinished() = "終了"
    override fun boardMoves() = "ターン進行度"
    override fun boardLatestMove() = "最近着手位置"

}
