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

    override fun startErrorSessionAlready(user: String) =
        "$user さん, ゲーム生成に失敗しました。今のゲームを終了してください。 :thinking:"
    override fun setErrorIllegalArgument(player: String) =
        "さん, 間違いコマンドです。`~s アルファベット 数字` 形式に書いてください。 :thinking:"
    override fun setErrorExist(player: String, move: String) = "$player さん, そこにはすでに碁石があります。 :thinking:"

    override fun beginPVP(owner: String, opponent: String, opener: String) =
        "`$owner`と `$opponent`のマッチが開始しました。先攻は `$opener`です。"

    override fun processNext(player: String, priorPlayer: String, latestMove: String) =
        "`$player`さん, 次の手を置いてください。 `$priorPlayer`は $latestMove に置きました。"

    override fun processErrorOrder(user: String, player: String) =
        "今は`$player`'さんの番です。`$player`さんの次の番を待ってください。 :thinking:"
    override fun endPVPWin(winner: String, looser: String, latestMove: String) =
        "`$winner`さんが $latestMove に置いて`$looser`さんに勝ちました。"
    override fun endPVPResign(winner: String, looser: String) =
        "`$looser`さんが降伏を宣言して`$winner`さんが勝ちました。"

    override fun endPVEWin(player: String, latestPos: String) = latestPos + "に置いてAIに勝ちました。おめでとうございます。"
    override fun endPVELose(player: String, latestPos: String) = "AIが" + latestPos + "に置いて負けました。"
    override fun endPVEResign(player: String) = "降伏を宣言してAIに負けました。"

    override fun endPVPTie(owner: String, opponent: String) = "もう碁石を置く所が無くて無勝負に処理されました。"

    override fun boardInProgress() = "進行中"
    override fun boardFinished() = "終了"
    override fun boardMoves() = "ターン進行度"
    override fun boardLatestMove() = "最近着手位置"

}
